package app.rtcmeetings.webrtc.video

import org.webrtc.VideoFrame
import org.webrtc.VideoSink

class ProxyVideoSink : VideoSink {
    var target : VideoSink?=null
    override fun onFrame(p0: VideoFrame?) {
        target?.onFrame(p0)
    }
}