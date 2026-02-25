package com.example.siwika

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.siwika.databinding.FragmentCameraBinding
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(), GestureRecognizerHelper.GestureRecognizerListener {

    private var _fragmentCameraBinding: FragmentCameraBinding? = null
    private val fragmentCameraBinding get() = _fragmentCameraBinding!!

    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper
    private val viewModel: MainViewModel by activityViewModels()

    // --- Practice Logic States ---
    private val signList = listOf("Thank you", "Hi", "Sorry", "Welcome", "Take care")
    private var currentTargetSign = ""
    private var streakCount = 0
    private var isProcessingMatch = false

    // --- Hold Timer States ---
    private var holdStartTime: Long = 0
    private val HOLD_DURATION = 3000L // 3 Seconds
    private var isHolding = false

    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var backgroundExecutor: ExecutorService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
        return fragmentCameraBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backgroundExecutor = Executors.newSingleThreadExecutor()

        pickNextRandomSign()

        fragmentCameraBinding.viewFinder.post { setUpCamera() }

        backgroundExecutor.execute {
            gestureRecognizerHelper = GestureRecognizerHelper(
                context = requireContext(),
                runningMode = RunningMode.LIVE_STREAM,
                minHandDetectionConfidence = viewModel.currentMinHandDetectionConfidence,
                minHandTrackingConfidence = viewModel.currentMinHandTrackingConfidence,
                minHandPresenceConfidence = viewModel.currentMinHandPresenceConfidence,
                currentDelegate = viewModel.currentDelegate,
                gestureRecognizerListener = this
            )
        }
    }

    private fun pickNextRandomSign() {
        val oldSign = currentTargetSign
        currentTargetSign = signList.filter { it != oldSign }.random()

        activity?.runOnUiThread {
            fragmentCameraBinding.tvPrompt.text = "Hold the sign for: '$currentTargetSign'"
            resetHoldTimer()
        }
    }

    override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {
        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null) {
                val gestureResult = resultBundle.results.first()
                val categories = gestureResult.gestures()

                // 1. Draw Skeleton Overlay
                if (gestureResult.landmarks().isNotEmpty()) {
                    fragmentCameraBinding.overlay.setResults(
                        gestureResult,
                        resultBundle.inputImageHeight,
                        resultBundle.inputImageWidth,
                        RunningMode.LIVE_STREAM
                    )
                } else {
                    fragmentCameraBinding.overlay.clear()
                }

                // 2. Logic: Check for Match & Process Timer
                // Added safety check for categories.first().isNotEmpty()
                if (categories.isNotEmpty() && categories.first().isNotEmpty() && !isProcessingMatch) {
                    val detectedName = categories.first().first().categoryName()

                    // Normalize for comparison
                    val normalizedDetected = detectedName.replace("_", " ").lowercase().trim()
                    val normalizedTarget = currentTargetSign.replace("_", " ").lowercase().trim()

                    if (normalizedDetected == normalizedTarget) {
                        processHoldTimer()
                    } else {
                        resetHoldTimer()
                    }
                } else {
                    resetHoldTimer()
                }

                fragmentCameraBinding.overlay.invalidate()
            }
        }
    }

    private fun processHoldTimer() {
        if (!isHolding) {
            isHolding = true
            holdStartTime = System.currentTimeMillis()
            fragmentCameraBinding.holdProgress.visibility = View.VISIBLE
        }

        val elapsed = System.currentTimeMillis() - holdStartTime
        fragmentCameraBinding.holdProgress.progress = elapsed.toInt()

        if (elapsed >= HOLD_DURATION) {
            isProcessingMatch = true
            handleMatchSuccess()
        }
    }

    private fun resetHoldTimer() {
        isHolding = false
        holdStartTime = 0
        fragmentCameraBinding.holdProgress.progress = 0
        fragmentCameraBinding.holdProgress.visibility = View.INVISIBLE
    }

    private fun handleMatchSuccess() {
        streakCount++
        fragmentCameraBinding.tvScoreCount.text = streakCount.toString()

        // Hide progress bar immediately
        fragmentCameraBinding.holdProgress.visibility = View.INVISIBLE

        // Give immediate feedback on the main prompt
        fragmentCameraBinding.tvPrompt.text = "Correct! Streak: $streakCount"

        // Delay before picking next sign
        fragmentCameraBinding.root.postDelayed({
            isProcessingMatch = false
            pickNextRandomSign()
        }, 2000)
    }

    // --- Standard CameraX Setup ---

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return
        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()

        val preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build()
        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(backgroundExecutor) { image ->
                    if(this::gestureRecognizerHelper.isInitialized) {
                        gestureRecognizerHelper.recognizeLiveStream(image)
                    }
                }
            }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
        preview.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()
        backgroundExecutor.shutdown()
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }
}