package app.rtcmeetings.webrtc

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class CallService : Service(), PeerConnectionEvents {

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        internal val service: CallService
            get() = this@CallService
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onLocalDescriptionSet(sdp: SessionDescription) {
    }

    override fun onLocalIceCandidate(candidate: IceCandidate) {
    }

    override fun onLocalIceCandidatesRemoved(candidates: Array<IceCandidate>) {
    }

    override fun onIceConnected() {
    }

    override fun onIceDisconnected() {
    }

    override fun onConnected() {
    }

    override fun onDisconnected() {
    }

    override fun onPeerConnectionError(description: String) {
    }

    override fun onRemoteDescriptionSet(remoteSdp: SessionDescription) {
    }

}