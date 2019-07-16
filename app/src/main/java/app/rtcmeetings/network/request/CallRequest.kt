package app.rtcmeetings.network.request

import com.google.gson.annotations.SerializedName

class CallRequest {
    @SerializedName("socketID")
    var socketId: String? = null
    @SerializedName("sdp")
    var sdp: String? = null
    @SerializedName("accountID")
    var userId: Int? = null
    @SerializedName("callID")
    var callId: Int? = null
    @SerializedName("event")
    var event: String? = null
    @SerializedName("payload")
    var payload: String? = null
}
