package app.rtcmeetings.network.request

import com.google.gson.annotations.SerializedName

data class IceExchange(
        @SerializedName("sdpMid") val sdpMid: String,
        @SerializedName("sdpMLineIndex") val mLineIndex: Int,
        @SerializedName("sdp") val sdp: String
)