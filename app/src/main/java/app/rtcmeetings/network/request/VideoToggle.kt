package app.rtcmeetings.network.request

import com.google.gson.annotations.SerializedName

data class VideoToggle(
    @SerializedName("state")
    val state: Boolean
)