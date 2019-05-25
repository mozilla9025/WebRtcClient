package app.rtcmeetings.webrtc

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface PeerConnectionEvents {

    /**
     * Callback fired once local SDP is created and set.
     */
    fun onLocalDescriptionSet(sdp: SessionDescription)

    /**
     * Callback fired once local Ice candidate is generated.
     */
    fun onLocalIceCandidate(candidate: IceCandidate)

    /**
     * Callback fired once local ICE candidates are removed.
     */
    fun onLocalIceCandidatesRemoved(candidates: Array<IceCandidate>)

    /**
     * Callback fired once connection is established (IceConnectionState is
     * CONNECTED).
     */
    fun onIceConnected()

    /**
     * Callback fired once connection is disconnected (IceConnectionState is
     * DISCONNECTED).
     */
    fun onIceDisconnected()

    /**
     * Callback fired once DTLS connection is established (PeerConnectionState
     * is CONNECTED).
     */
    fun onConnected()

    /**
     * Callback fired once DTLS connection is disconnected (PeerConnectionState
     * is DISCONNECTED).
     */
    fun onDisconnected()

    /**
     * Callback fired once peer connection error happened.
     */
    fun onPeerConnectionError(description: String)

    fun onRemoteDescriptionSet(remoteSdp: SessionDescription)
}
