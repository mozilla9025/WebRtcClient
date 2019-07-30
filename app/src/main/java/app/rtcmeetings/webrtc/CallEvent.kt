package app.rtcmeetings.webrtc

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import app.rtcmeetings.data.entity.User

object CallEvent {

    fun startCall(context: Context, user: User, socketId: String) {
        val intent = Intent(context, CallService::class.java)
            .apply {
                action = CallService.ACTION_OUTGOING_CALL
                putExtra(CallService.EXTRA_USER, user)
                putExtra(CallService.EXTRA_STRING, socketId)
            }
        ContextCompat.startForegroundService(context, intent)
    }

    fun localEnd(context: Context) {
        ContextCompat.startForegroundService(
            context,
            getIntent(context, CallService.ACTION_LOCAL_FINISH)
        )
    }

    fun localCancel(context: Context) {
        ContextCompat.startForegroundService(
            context,
            getIntent(context, CallService.ACTION_LOCAL_CANCEL)
        )
    }

    fun localDecline(context: Context) {
        ContextCompat.startForegroundService(
            context,
            getIntent(context, CallService.ACTION_LOCAL_DECLINE)
        )
    }

    fun stopIncomingRinger(context: Context) {
        ContextCompat.startForegroundService(
            context,
            getIntent(context, CallService.ACTION_STOP_INCOMING_RINGER)
        )
    }

    fun acceptIncomingCall(context: Context) {
        ContextCompat.startForegroundService(
            context,
            getIntent(context, CallService.ACTION_LOCAL_ACCEPT)
        )
    }

    fun localCamToggle(context: Context) {
        ContextCompat.startForegroundService(
            context,
            getIntent(context, CallService.ACTION_LOCAL_VIDEO_TOGGLE)
        )
    }

    fun localMicToggle(context: Context) {
        ContextCompat.startForegroundService(
            context,
            getIntent(context, CallService.ACTION_LOCAL_TOGGLE_MICROPHONE)
        )
    }

    fun localCamSwitch(context: Context) {
        ContextCompat.startForegroundService(
            context,
            getIntent(context, CallService.ACTION_LOCAL_SWITCH_CAMERA)
        )
    }

    fun localSpeakerToggle(context: Context, enabled: Boolean) {
        ContextCompat.startForegroundService(context,
            getIntent(context, CallService.ACTION_LOCAL_TOGGLE_SPEAKER)
                .apply {
                    putExtra(CallService.EXTRA_BOOLEAN, enabled)
                })
    }

    fun onCall(context: Context, args: String, socketId: String) {
        ContextCompat.startForegroundService(context,
            getIntent(context, CallService.ACTION_INCOMING_CALL)
                .apply {
                    putExtra(CallService.EXTRA_STRING, args)
                    putExtra(CallService.EXTRA_SOCKET_ID, socketId)
                })
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

    fun terminate(context: Context) {
        ContextCompat.startForegroundService(
            context,
            getIntent(context, CallService.ACTION_TERMINATE)
        )
    }

    fun onSocketIdChange(context: Context, socketId: String) {
        ContextCompat.startForegroundService(
            context,
            getIntent(context, CallService.ACTION_SOCKET_ID_CHANGED)
                .apply { putExtra(CallService.EXTRA_SOCKET_ID, socketId) }
        )
    }

    private fun getIntent(context: Context, act: String): Intent {
        return Intent(context, CallService::class.java).apply {
            action = act
        }
    }
}