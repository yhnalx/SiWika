package com.example.siwika

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: GestureRecognizerResult? = null

    // Modernized Paints
    private val linePaint = Paint()
    private val pointPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        results = null
        invalidate()
    }

    private fun initPaints() {
        // Line Paint: Bubbly Orange with rounded edges
        linePaint.apply {
            color = Color.parseColor("#FBB040") // Your SiWika Orange
            strokeWidth = LANDMARK_STROKE_WIDTH
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND // Makes joints look soft
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true

            // Subtle glow effect
            setShadowLayer(8f, 0f, 0f, Color.parseColor("#4D000000"))
        }

        // Point Paint: White dots for the joints to create contrast
        pointPaint.apply {
            color = Color.WHITE
            strokeWidth = POINT_RADIUS
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Ensure shadow layer is drawn on hardware acceleration
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        results?.let { gestureRecognizerResult ->
            for (landmarks in gestureRecognizerResult.landmarks()) {

                // 1. Draw Connections First (The Skeleton)
                HandLandmarker.HAND_CONNECTIONS.forEach { connection ->
                    val start = landmarks[connection!!.start()]
                    val end = landmarks[connection.end()]

                    canvas.drawLine(
                        start.x() * imageWidth * scaleFactor,
                        start.y() * imageHeight * scaleFactor,
                        end.x() * imageWidth * scaleFactor,
                        end.y() * imageHeight * scaleFactor,
                        linePaint
                    )
                }

                // 2. Draw Points on top (The Joints)
                for (normalizedLandmark in landmarks) {
                    canvas.drawCircle(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        POINT_RADIUS,
                        pointPaint
                    )
                }
            }
        }
    }

    fun setResults(
        gestureRecognizerResult: GestureRecognizerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.LIVE_STREAM
    ) {
        results = gestureRecognizerResult
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> min(width * 1f / imageWidth, height * 1f / imageHeight)
            RunningMode.LIVE_STREAM -> max(width * 1f / imageWidth, height * 1f / imageHeight)
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F // Thicker for "bubbly" look
        private const val POINT_RADIUS = 8F // Smaller white joint dots
    }
}