package app.rtcmeetings.webrtc

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface WebRtcClientListener {

    fun onIceCandidate(candidate: IceCandidate)

    fun onAnswerGenerated(sdp: SessionDescription)

    fun onOfferGenerated(sdp: SessionDescription)

    fun onConnected()

    fun onDisconnected()

    fun onFailed()
}