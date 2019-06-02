package app.rtcmeetings.webrtc

import app.rtcmeetings.webrtc.video.CamSide

interface DeviceEventListener {

    fun onCamToggle(isEnabled: Boolean)

    fun onMicToggle(isEnabled: Boolean)

    fun onCamSwitch(camSide: CamSide)

}