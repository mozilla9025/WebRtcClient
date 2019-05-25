package app.rtcmeetings.webrtc

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class CallService : Service() {

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        internal val service: CallService
            get() = this@CallService
    }

    override fun onBind(intent: Intent?): IBinder? = binder
}