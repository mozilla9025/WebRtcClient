package app.rtcmeetings.webrtc

interface CallEventListener {

    fun onConnect()

    fun onDisconnect()

    fun onFail()

    fun onRemoteUserStopStream()

    fun onRemoteUserStartStream()

    fun onCreatingConnection()

    fun onFinish()

}