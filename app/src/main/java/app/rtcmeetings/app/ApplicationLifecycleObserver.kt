package app.rtcmeetings.app

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import app.rtcmeetings.network.ws.WsService

class ApplicationLifecycleObserver(
        private val app: ApplicationLoader
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppStart() {
        WsService.connect(app)
    }
}