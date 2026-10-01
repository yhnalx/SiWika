package com.example.siwika

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.mediapipe.tasks.vision.core.RunningMode

class MainViewModel : AndroidViewModel {

    companion object {
        private val TAG = MainViewModel::class.java.getSimpleName()
    }

    public var keepSplashAlive  : Boolean
    private val result : MutableLiveData<String>

    constructor(application : Application) : super(application) {
        keepSplashAlive = true
        result = MutableLiveData<String>()
        keepSplashAlive = false
    }
    //region Camera Permission Methods
    public fun checkCameraPermission(permissionResultResultLauncher: ActivityResultLauncher<String>, isGranted : () -> Unit = {}, isDenied : () -> Unit = {}) {
        ManifestPermission.checkSelfPermission (
            getApplication<Application>(),
            ManifestPermission.cameraPermission,
            isGranted = isGranted,
            isDenied = {
                isDenied()
                ManifestPermission.requestPermission (
                    permissionResultResultLauncher,
                    ManifestPermission.cameraPermission
                )
            }
        )
    }
    //endregion
    //region MP Gesture Recognizer
    private var _delegate: Int = GestureRecognizerHelper.DELEGATE_CPU
    private var _minHandDetectionConfidence: Float = GestureRecognizerHelper.DEFAULT_HAND_DETECTION_CONFIDENCE
    private var _minHandTrackingConfidence: Float = GestureRecognizerHelper.DEFAULT_HAND_TRACKING_CONFIDENCE
    private var _minHandPresenceConfidence: Float = GestureRecognizerHelper.DEFAULT_HAND_PRESENCE_CONFIDENCE
    val currentDelegate: Int get() = _delegate
    val currentMinHandDetectionConfidence: Float get() = _minHandDetectionConfidence
    val currentMinHandTrackingConfidence: Float get() = _minHandTrackingConfidence
    val currentMinHandPresenceConfidence: Float get() = _minHandPresenceConfidence
    fun setDelegate(delegate: Int?) {
        if (delegate != null) {
            _delegate = delegate
        }
    }
    fun setMinHandDetectionConfidence(confidence: Float?) {
        confidence?.let { _minHandDetectionConfidence = it }
    }
    fun setMinHandTrackingConfidence(confidence: Float?) {
        if (confidence != null) {
            _minHandTrackingConfidence = confidence
        }
    }
    fun setMinHandPresenceConfidence(confidence: Float?) {
        if (confidence != null) {
            _minHandPresenceConfidence = confidence
        }
    }
    //endreiogn
    override fun onCleared() {
        super.onCleared()
    }
}