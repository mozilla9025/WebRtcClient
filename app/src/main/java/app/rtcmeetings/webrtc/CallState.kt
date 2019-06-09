package app.rtcmeetings.webrtc

enum class CallState {

    INCOMING_PENDING,
    OUTGOING_PENDING,
    INCOMING_CONNECTING,
    OUTGOING_CONNECTING,
    CONNECTED,
    FAILED
}