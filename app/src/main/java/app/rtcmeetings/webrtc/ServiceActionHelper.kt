package app.rtcmeetings.webrtc

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

object ServiceActionHelper {

    fun terminate(context: Context) {
        ContextCompat.startForegroundService(
                context,
                getIntent(context, CallService.ACTION_TERMINATE)
        )
    }

    private fun getIntent(context: Context, serviceAction: String): Intent =
            Intent(context, CallService::class.java).apply {
                action = serviceAction
            }

    fun startOutgoingCall(context: Context, interlocutor: String?) {
        if (interlocutor == null) return

        val intent = getIntent(context, CallService.ACTION_START_OUTGOING_CALL).apply {
            putExtra(CallService.EXTRA_STRING, interlocutor)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun acceptIncomingCall(context: Context) {
        val intent = getIntent(context, CallService.ACTION_ACCEPT_INCOMING_CALL)
        ContextCompat.startForegroundService(context, intent)
    }

    fun declineIncomingCall(context: Context) {
        val intent = getIntent(context, CallService.ACTION_DECLINE_INCOMING_CALL)
        ContextCompat.startForegroundService(context, intent)
    }

    fun cancelOutgoingCall(context: Context) {
        val intent = getIntent(context, CallService.ACTION_CANCEL_OUTGOING_CALL)
        ContextCompat.startForegroundService(context, intent)
    }

    fun finishActiveCall(context: Context) {
        val intent = getIntent(context, CallService.ACTION_FINISH_CALL)
        ContextCompat.startForegroundService(context, intent)
    }

    fun toggleCam(context: Context) {
        val intent = getIntent(context, CallService.ACTION_LOCAL_TOGGLE_CAMERA)
        ContextCompat.startForegroundService(context, intent)
    }

    fun toggleMic(context: Context) {
        val intent = getIntent(context, CallService.ACTION_LOCAL_TOGGLE_MICROPHONE)
        ContextCompat.startForegroundService(context, intent)
    }

    fun switchCam(context: Context) {
        val intent = getIntent(context, CallService.ACTION_LOCAL_SWITCH_CAMERA)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopIncomingRinger(context: Context) {
        val intent = getIntent(context, CallService.ACTION_STOP_INCOMING_RINGER)
        ContextCompat.startForegroundService(context, intent)
    }
}