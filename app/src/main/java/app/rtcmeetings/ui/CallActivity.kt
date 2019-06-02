package app.rtcmeetings.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import app.rtcmeetings.R
import app.rtcmeetings.base.BaseActivity
import app.rtcmeetings.webrtc.CallService

class CallActivity : BaseActivity() {

    private var callService: CallService? = null

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            callService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            callService = (service as CallService.LocalBinder).service
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
    }

    override fun onStart() {
        super.onStart()
        bindCallService()
    }

    override fun onStop() {
        super.onStop()
        unbindCallService()
    }

    private fun bindCallService() {
        bindService(
            Intent(this@CallActivity, CallService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun unbindCallService() {
        unbindService(connection)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CallActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}