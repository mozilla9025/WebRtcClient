package app.rtcmeetings.webrtc

enum class CallState {

    INCOMING_PENDING,
    OUTGOING_PENDING,
    CONNECTING,
    CONNECTED,
    FAILED
}