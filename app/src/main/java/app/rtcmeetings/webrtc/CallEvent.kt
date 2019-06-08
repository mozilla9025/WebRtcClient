package app.rtcmeetings.webrtc

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

object CallEvent {

    fun onCall(context: Context, args: String) {
        ContextCompat.startForegroundService(context,
            getIntent(context, CallService.ACTION_START_INCOMING_CALL)
                .apply { putExtra(CallService.EXTRA_STRING, args) })
    }

    fun onAccept(context: Context, args: String) {
        ContextCompat.startForegroundService(context,
            getIntent(context, CallService.ACTION_REMOTE_ACCEPTED)
                .apply { putExtra(CallService.EXTRA_STRING, args) })
    }

    fun onDecline(context: Context, args: String) {
        ContextCompat.startForegroundService(context,
            getIntent(context, CallService.ACTION_REMOTE_DECLINE)
                .apply { putExtra(CallService.EXTRA_STRING, args) })
    }

    fun onCancel(context: Context, args: String) {
        ContextCompat.startForegroundService(context,
            getIntent(context, CallService.ACTION_REMOTE_CANCEL)
                .apply { putExtra(CallService.EXTRA_STRING, args) })
    }

    fun onMiss(context: Context, args: String) {
        //NO-OP
    }

    fun onFinish(context: Context, args: String) {
        ContextCompat.startForegroundService(context,
            getIntent(context, CallService.ACTION_REMOTE_FINISH)
                .apply { putExtra(CallService.EXTRA_STRING, args) })
    }

    fun onIce(context: Context, args: String) {
        ContextCompat.startForegroundService(context,
            getIntent(context, CallService.ACTION_REMOTE_ICE)
                .apply { putExtra(CallService.EXTRA_STRING, args) })
    }

    fun onVideoToggle(context: Context, args: String) {
        ContextCompat.startForegroundService(context,
            getIntent(context, CallService.ACTION_REMOTE_VIDEO_TOGGLE)
                .apply { putExtra(CallService.EXTRA_STRING, args) })
    }

    private fun getIntent(context: Context, act: String): Intent {
        return Intent(context, CallService::class.java).apply {
            action = act
        }
    }
}