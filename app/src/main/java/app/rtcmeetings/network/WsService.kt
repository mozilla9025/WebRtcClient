package app.rtcmeetings.network

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class WsService : Service() {

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        internal val service: WsService
            get() = this@WsService
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }


}