package app.rtcmeetings.webrtc.video

import android.content.Context
import org.webrtc.*

class CamCapturer private constructor() {

    lateinit var capturer: VideoCapturer
    lateinit var cameraSide: CamSide

    private constructor(capturer: CameraVideoCapturer, cameraSide: CamSide) : this() {
        this.capturer = capturer
        this.cameraSide = cameraSide
    }

    fun switchCamera() {
        (capturer as CameraVideoCapturer).switchCamera(null)
        cameraSide = when (cameraSide) {
            CamSide.FRONT_FACING -> CamSide.BACK_FACING
            CamSide.BACK_FACING -> CamSide.FRONT_FACING
        }
    }

    fun dispose() {
        capturer.dispose()
    }

    @Throws(InterruptedException::class)
    fun stopCapture() {
        capturer.stopCapture()
    }

    companion object {
        fun create(context: Context): CamCapturer =
                when {
                    Camera2Enumerator.isSupported(context) -> createCameraCapturer(Camera2Enumerator(context))!!
                    else -> createCameraCapturer(Camera1Enumerator(false))!!
                }

        private fun createCameraCapturer(enumerator: CameraEnumerator): CamCapturer? {
            val deviceNames = enumerator.deviceNames
            deviceNames.forEach { deviceName ->
                if (enumerator.isFrontFacing(deviceName)) {
                    val videoCapturer = enumerator.createCapturer(deviceName, null)
                    if (videoCapturer != null)
                        return CamCapturer(videoCapturer, CamSide.FRONT_FACING)
                }
            }

            deviceNames.forEach { deviceName ->
                if (!enumerator.isFrontFacing(deviceName)) {
                    val videoCapturer = enumerator.createCapturer(deviceName, null)
                    if (videoCapturer != null)
                        return CamCapturer(videoCapturer, CamSide.BACK_FACING)
                }
            }
            return null
        }
    }
}
