package app.rtcmeetings.network.ws

object WsEvent {
    const val NEW_CALL = "new_call"
    const val ACCEPT = "accept_call"
    const val DECLINE = "decline_call"
    const val CANCEL = "cancel_call"
    const val MISS = "miss_call"
    const val FINISH = "finish_call"
    const val ICE_EXCHANGE = "ice_exchange"
    const val VIDEO_TOGGLE = "video_toggle"
}