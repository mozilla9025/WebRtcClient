package app.rtcmeetings.webrtc

import android.content.Context
import android.util.Log
import app.rtcmeetings.util.logd
import app.rtcmeetings.util.loge
import app.rtcmeetings.util.logi
import app.rtcmeetings.util.logw

import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.CameraVideoCapturer
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.Logging
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.IceConnectionState
import org.webrtc.PeerConnection.PeerConnectionState
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpParameters
import org.webrtc.RtpReceiver
import org.webrtc.RtpSender
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoDecoderFactory
import org.webrtc.VideoEncoderFactory
import org.webrtc.VideoSink
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import org.webrtc.audio.AudioDeviceModule
import org.webrtc.audio.JavaAudioDeviceModule
import org.webrtc.audio.JavaAudioDeviceModule.AudioRecordErrorCallback
import org.webrtc.audio.JavaAudioDeviceModule.AudioTrackErrorCallback

import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.regex.Matcher
import java.util.regex.Pattern

class WebRtcClient(
    private val appContext: Context,
    private val rootEglBase: EglBase,
    private val peerConnectionParameters: PeerConnectionParameters,
    private val events: PeerConnectionEvents?
) {

    private val pcObserver = PCObserver()
    private val sdpObserver = SDPObserver()
    private var factory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var audioSource: AudioSource? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var videoSource: VideoSource? = null
    private var preferIsac: Boolean = false
    private var videoCapturerStopped: Boolean = false
    private var isError: Boolean = false
    private var localRender: VideoSink? = null
    private var remoteSinks: MutableList<VideoSink>? = null
    private var iceServers: MutableList<PeerConnection.IceServer>? = null
    private var videoWidth: Int = 0
    private var videoHeight: Int = 0
    private var videoFps: Int = 0
    private var audioConstraints: MediaConstraints? = null
    private var sdpMediaConstraints: MediaConstraints? = null
    private var isInitiator: Boolean = false
    private var localSdp: SessionDescription? = null // either offer or answer SDP
    private var remoteSdp: SessionDescription? = null
    private var videoCapturer: VideoCapturer? = null
    private var renderVideo = true
    private var localVideoTrack: VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var localVideoSender: RtpSender? = null
    private var enableAudio = true
    private var localAudioTrack: AudioTrack? = null

    val isHDVideo: Boolean
        get() = videoWidth * videoHeight >= 1280 * 720

    /**
     * Peer connection parameters.
     */
    class PeerConnectionParameters(internal val videoCallEnabled: Boolean) {
        internal val videoWidth: Int
        internal val videoHeight: Int
        internal val videoFps: Int
        internal val videoMaxBitrate: Int
        internal val videoCodec: String
        internal val videoCodecHwAcceleration: Boolean
        internal val videoFlexfecEnabled: Boolean
        internal val audioStartBitrate: Int
        internal val audioCodec: String?
        internal val noAudioProcessing: Boolean
        internal val disableBuiltInAEC: Boolean
        internal val disableBuiltInAGC: Boolean
        internal val disableBuiltInNS: Boolean
        internal val disableWebRtcAGCAndHPF: Boolean

        init {
            this.videoWidth = HD_VIDEO_WIDTH
            this.videoHeight = HD_VIDEO_HEIGHT
            this.videoFps = 0
            this.videoMaxBitrate = 0
            this.videoCodec = VIDEO_CODEC_VP8
            this.videoFlexfecEnabled = false
            this.videoCodecHwAcceleration = true
            this.audioStartBitrate = 32
            this.audioCodec = AUDIO_CODEC_OPUS
            this.noAudioProcessing = false
            this.disableBuiltInAEC = false
            this.disableBuiltInAGC = false
            this.disableBuiltInNS = false
            this.disableWebRtcAGCAndHPF = false
        }
    }

    init {
        logd("Preferred video codec: " + getSdpVideoCodecName(peerConnectionParameters))
        val fieldTrials = getFieldTrials(peerConnectionParameters)
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(appContext)
                .setFieldTrials(fieldTrials)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )
    }

    /**
     * This function should only be called once.
     */
    fun createPeerConnectionFactory(options: PeerConnectionFactory.Options) {
        if (factory != null) {
            throw IllegalStateException("PeerConnectionFactory has already been constructed")
        }
        createPeerConnectionFactoryInternal(options)
    }

    fun createPeerConnection(
        localRender: VideoSink,
        remoteSink: VideoSink,
        videoCapturer: VideoCapturer?,
        iceServers: MutableList<PeerConnection.IceServer>
    ) {

        if (peerConnectionParameters.videoCallEnabled && videoCapturer == null) {
            logw("Video call enabled but no video capturer provided.")
        }
        createPeerConnection(localRender, mutableListOf(remoteSink), videoCapturer, iceServers)
    }

    fun createPeerConnection(
        localRender: VideoSink,
        remoteSinks: MutableList<VideoSink>,
        videoCapturer: VideoCapturer?,
        iceServers: MutableList<PeerConnection.IceServer>
    ) {

        this.localRender = localRender
        this.remoteSinks = remoteSinks
        this.iceServers = iceServers
        this.videoCapturer = videoCapturer
        try {
            createMediaConstraintsInternal()
            createPeerConnectionInternal()
        } catch (e: Exception) {
            reportError("Failed to create peer connection: " + e.message)
            throw e
        }

    }

    fun close() {
        closeInternal()
    }

    private fun createPeerConnectionFactoryInternal(options: PeerConnectionFactory.Options?) {
        isError = false

        // Check if ISAC is used by default.
        preferIsac =
            peerConnectionParameters.audioCodec != null && peerConnectionParameters.audioCodec == AUDIO_CODEC_ISAC

        val adm = createAudioDevice()
        // Create peer connection factory.
        if (options != null) {
            logd("Factory networkIgnoreMask option: " + options.networkIgnoreMask)
        }
        val enableH264HighProfile = VIDEO_CODEC_H264_HIGH == peerConnectionParameters.videoCodec

        val encoderFactory = DefaultVideoEncoderFactory(
            rootEglBase.eglBaseContext,
            true, enableH264HighProfile
        )

        val decoderFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)
        factory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setAudioDeviceModule(adm)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
        logd("Peer connection factory created.")
        adm.release()
    }

    private fun createAudioDevice(): AudioDeviceModule {
        val audioRecordErrorCallback = object : AudioRecordErrorCallback {
            override fun onWebRtcAudioRecordInitError(errorMessage: String) {
                loge("onWebRtcAudioRecordInitError: $errorMessage")
                reportError(errorMessage)
            }

            override fun onWebRtcAudioRecordStartError(
                errorCode: JavaAudioDeviceModule.AudioRecordStartErrorCode, errorMessage: String
            ) {
                loge("onWebRtcAudioRecordStartError: $errorCode. $errorMessage")
                reportError(errorMessage)
            }

            override fun onWebRtcAudioRecordError(errorMessage: String) {
                loge("onWebRtcAudioRecordError: $errorMessage")
                reportError(errorMessage)
            }
        }
        val audioTrackErrorCallback = object : AudioTrackErrorCallback {
            override fun onWebRtcAudioTrackInitError(errorMessage: String) {
                loge("onWebRtcAudioTrackInitError: $errorMessage")
                reportError(errorMessage)
            }

            override fun onWebRtcAudioTrackStartError(
                errorCode: JavaAudioDeviceModule.AudioTrackStartErrorCode, errorMessage: String
            ) {
                loge("onWebRtcAudioTrackStartError: $errorCode. $errorMessage")
                reportError(errorMessage)
            }

            override fun onWebRtcAudioTrackError(errorMessage: String) {
                loge("onWebRtcAudioTrackError: $errorMessage")
                reportError(errorMessage)
            }
        }
        return JavaAudioDeviceModule.builder(appContext)
            .setUseHardwareAcousticEchoCanceler(!peerConnectionParameters!!.disableBuiltInAEC)
            .setUseHardwareNoiseSuppressor(!peerConnectionParameters.disableBuiltInNS)
            .setAudioRecordErrorCallback(audioRecordErrorCallback)
            .setAudioTrackErrorCallback(audioTrackErrorCallback)
            .createAudioDeviceModule()
    }

    private fun createMediaConstraintsInternal() {

        videoWidth = peerConnectionParameters!!.videoWidth
        videoHeight = peerConnectionParameters.videoHeight
        videoFps = peerConnectionParameters.videoFps
        // If video resolution is not specified, default to HD.
        if (videoWidth == 0 || videoHeight == 0) {
            videoWidth = HD_VIDEO_WIDTH
            videoHeight = HD_VIDEO_HEIGHT
        }
        // If fps is not specified, default to 30.
        if (videoFps == 0) {
            videoFps = 30
        }
        Logging.d(TAG, "Capturing format: " + videoWidth + "x" + videoHeight + "@" + videoFps)
        // Create audio constraints.
        audioConstraints = MediaConstraints()
        // added for audio performance measurements
        if (peerConnectionParameters.noAudioProcessing) {
            logd("Disabling audio processing")
            audioConstraints!!.mandatory.add(
                MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false")
            )
            audioConstraints!!.mandatory.add(
                MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false")
            )
            audioConstraints!!.mandatory.add(
                MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false")
            )
            audioConstraints!!.mandatory.add(
                MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false")
            )
        }
        // Create SDP constraints.
        sdpMediaConstraints = MediaConstraints()
        sdpMediaConstraints!!.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        sdpMediaConstraints!!.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        sdpMediaConstraints!!.optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))

        audioConstraints!!.optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
    }

    private fun createPeerConnectionInternal() {
        if (factory == null || isError) {
            loge("Peerconnection factory is not created")
            return
        }
        logd("Create peer connection.")
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXCOMPAT
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.NEGOTIATE
        //        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        rtcConfig.iceTransportsType = PeerConnection.IceTransportsType.ALL
        //        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA
        //        // Enable DTLS for normal calls and disable for loopback calls.
        //        //HARDCODED
        rtcConfig.enableDtlsSrtp = true
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        peerConnection = factory!!.createPeerConnection(rtcConfig, pcObserver)
        isInitiator = false
        // Set INFO libjingle logging.
        // NOTE: this _must_ happen while |factory| is alive!
        Logging.enableLogToDebugOutput(Logging.Severity.LS_INFO)
        val mediaStreamLabels = listOf("ARDAMS")
        if (videoCapturer != null) {
            if (peerConnection != null) {
                peerConnection!!.addTrack(createVideoTrack(videoCapturer!!), mediaStreamLabels)
            }
        }
        // We can handleLicenseResponse the renderers right away because we don't need to wait for an
        // answer to get the remote track.
        remoteVideoTrack = getRemoteVideoTrack()
        if (remoteVideoTrack != null) {
            remoteVideoTrack!!.setEnabled(renderVideo)
            for (remoteSink in remoteSinks!!) {
                remoteVideoTrack!!.addSink(remoteSink)
            }
        }
        peerConnection!!.addTrack(createAudioTrack(), mediaStreamLabels)
        findVideoSender()

        stopVideoSource()
        logd("Peer connection created.")
    }

    @Throws(RuntimeException::class)
    private fun closeInternal() {
        logd("Closing peer connection.")

        if (peerConnection != null) {
            peerConnection!!.dispose()
            peerConnection = null
        }
        logd("Closing audio source.")
        if (audioSource != null) {
            audioSource!!.dispose()
            audioSource = null
        }
        logd("Stopping capture.")
        if (videoCapturer != null) {
            try {
                videoCapturer!!.stopCapture()
            } catch (e: InterruptedException) {
                loge("closeInternal: ", e)
            }

            videoCapturerStopped = true
            videoCapturer!!.dispose()
            videoCapturer = null
        }
        logd("Closing video source.")
        if (videoSource != null) {
            videoSource!!.dispose()
            videoSource = null
        }
        if (surfaceTextureHelper != null) {
            surfaceTextureHelper!!.dispose()
            surfaceTextureHelper = null
        }

        localRender = null
        remoteSinks = null
        logd("Closing peer connection factory.")
        if (factory != null) {
            factory!!.dispose()
            factory = null
        }
        PeerConnectionFactory.stopInternalTracingCapture()
        PeerConnectionFactory.shutdownInternalTracer()
        logd("Closing peer connection done.")
    }

    fun setAudioEnabled(enable: Boolean) {
        enableAudio = enable
        if (localAudioTrack != null) {
            localAudioTrack!!.setEnabled(enableAudio)
        }
    }

    fun setVideoEnabled(enable: Boolean) {
        renderVideo = enable
        if (localVideoTrack != null) {
            localVideoTrack!!.setEnabled(renderVideo)
        }
        if (remoteVideoTrack != null) {
            remoteVideoTrack!!.setEnabled(renderVideo)
        }
    }

    fun createOffer() {
        if (peerConnection != null && !isError) {
            logd("PC Create OFFER")
            isInitiator = true
            peerConnection!!.createOffer(sdpObserver, sdpMediaConstraints)
        }
    }

    fun createAnswer() {
        if (peerConnection != null && !isError) {
            logd("PC create ANSWER")
            isInitiator = false
            peerConnection!!.createAnswer(sdpObserver, sdpMediaConstraints)
        }
    }

    fun addRemoteIceCandidate(candidate: IceCandidate) {
        if (peerConnection != null && !isError) {
            logi("addRemoteIceCandidate: $candidate")
            peerConnection!!.addIceCandidate(candidate)
        }
    }

    fun removeRemoteIceCandidates(candidates: Array<IceCandidate>) {
        //        executor.execute(() -> {
        if (peerConnection == null || isError) {
            return
        }
        // Drain the queued remote candidates if there is any so that
        // they are processed in the proper order.
        //        drainCandidates();
        peerConnection!!.removeIceCandidates(candidates)
        //        });
    }

    fun setRemoteDescription(sdp: SessionDescription) {
        if (peerConnection == null || isError) {
            return
        }
        val remoteSession = SessionDescription(sdp.type, sdp.description)

        var sdpDescription = remoteSession.description
        if (preferIsac) {
            sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC, true)
        }
        sdpDescription = preferCodec(sdpDescription, getSdpVideoCodecName(peerConnectionParameters!!), false)

        if (peerConnectionParameters.audioStartBitrate > 0) {
            sdpDescription = setStartBitrate(
                AUDIO_CODEC_OPUS, false,
                sdpDescription, peerConnectionParameters.audioStartBitrate
            )
        }

        remoteSdp = SessionDescription(remoteSession.type, sdpDescription)

        peerConnection!!.setRemoteDescription(sdpObserver, remoteSdp)
        logd("Set remote SDP.")
    }

    fun stopVideoSource() {
        if (videoCapturer != null && !videoCapturerStopped) {
            logd("Stop video source.")
            try {
                videoCapturer!!.stopCapture()
            } catch (e: InterruptedException) {
            }

            videoCapturerStopped = true
        }
    }

    fun startVideoSource() {
        if (videoCapturer != null && videoCapturerStopped) {
            logd("Restart video source.")
            videoCapturer!!.startCapture(videoWidth, videoHeight, videoFps)
            videoCapturerStopped = false
        }
    }

    fun setVideoMaxBitrate(maxBitrateKbps: Int?) {
        if (peerConnection == null || localVideoSender == null || isError) {
            return
        }
        logd("Requested max video bitrate: " + maxBitrateKbps!!)
        if (localVideoSender == null) {
            logw("Sender is not ready.")
            return
        }
        val parameters = localVideoSender!!.parameters
        if (parameters.encodings.size == 0) {
            logw("RtpParameters are not ready.")
            return
        }
        for (encoding in parameters.encodings) {
            // Null value means no limit.
            encoding.maxBitrateBps = maxBitrateKbps * BPS_IN_KBPS
        }
        if (!localVideoSender!!.setParameters(parameters)) {
            loge("RtpSender.setParameters failed.")
        }
        logd("Configured max video bitrate to: $maxBitrateKbps")
    }

    private fun reportError(errorMessage: String) {
        loge("Peerconnection error: $errorMessage")
        if (!isError) {
            events?.onPeerConnectionError(errorMessage)
            isError = true
        }
    }

    private fun createAudioTrack(): AudioTrack {
        audioSource = factory!!.createAudioSource(audioConstraints)
        localAudioTrack = factory!!.createAudioTrack(AUDIO_TRACK_ID, audioSource!!)
        localAudioTrack!!.setEnabled(enableAudio)
        return localAudioTrack!!
    }

    private fun createVideoTrack(capturer: VideoCapturer): VideoTrack {
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.eglBaseContext)
        videoSource = factory!!.createVideoSource(capturer.isScreencast)
        capturer.initialize(surfaceTextureHelper, appContext, videoSource!!.capturerObserver)
        capturer.startCapture(videoWidth, videoHeight, videoFps)
        localVideoTrack = factory!!.createVideoTrack(VIDEO_TRACK_ID, videoSource!!)
        localVideoTrack!!.setEnabled(renderVideo)
        localVideoTrack!!.addSink(localRender!!)
        return localVideoTrack!!
    }

    private fun findVideoSender() {
        if (peerConnection == null) return
        for (sender in peerConnection!!.senders) {
            if (sender.track() != null) {
                val trackType = sender.track()!!.kind()
                if (trackType == VIDEO_TRACK_TYPE) {
                    logd("Found video sender.")
                    localVideoSender = sender
                }
            }
        }
    }

    // Returns the remote VideoTrack, assuming there is only one.
    private fun getRemoteVideoTrack(): VideoTrack? {
        if (peerConnection == null) return null
        for (transceiver in peerConnection!!.transceivers) {
            val track = transceiver.receiver.track()
            if (track is VideoTrack) {
                return track
            }
        }
        return null
    }

    private fun switchCameraInternal() {
        if (videoCapturer is CameraVideoCapturer) {
            if (isError) {
                loge("Failed to switch camera. Error : $isError")
                return  // No video is sent or only one camera is available or error happened.
            }
            logd("Switch camera")
            val cameraVideoCapturer = videoCapturer as CameraVideoCapturer?
            cameraVideoCapturer!!.switchCamera(null)
        } else {
            logd("Will not switch camera, video caputurer is not a camera")
        }
    }

    fun switchCamera() {
        switchCameraInternal()
        //        executor.execute(this::switchCameraInternal);
    }

    private inner class PCObserver : PeerConnection.Observer {
        override fun onIceCandidate(candidate: IceCandidate) {
            events!!.onLocalIceCandidate(candidate)
        }

        override fun onIceCandidatesRemoved(candidates: Array<IceCandidate>) {
            events!!.onLocalIceCandidatesRemoved(candidates)
        }

        override fun onSignalingChange(newState: PeerConnection.SignalingState) {
            logd("SignalingState: $newState")
        }

        override fun onIceConnectionChange(newState: IceConnectionState) {
            logd("IceConnectionState: $newState")
            when (newState) {
                IceConnectionState.CONNECTED -> events!!.onIceConnected()
                IceConnectionState.DISCONNECTED -> events!!.onIceDisconnected()
                IceConnectionState.FAILED -> reportError("ICE connection failed.")
            }
        }

        override fun onConnectionChange(newState: PeerConnectionState?) {
            logd("PeerConnectionState: " + newState!!)
            when (newState) {
                PeerConnectionState.CONNECTED -> events?.onConnected()
                PeerConnectionState.DISCONNECTED -> events?.onDisconnected()
                PeerConnectionState.FAILED -> reportError("DTLS connection failed.")
            }
        }

        override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) {
            logd("IceGatheringState: $newState")
        }

        override fun onIceConnectionReceivingChange(receiving: Boolean) {
            logd("IceConnectionReceiving changed to $receiving")
        }

        override fun onAddStream(stream: MediaStream) {}

        override fun onRemoveStream(stream: MediaStream) {
            remoteVideoTrack = null
        }

        override fun onDataChannel(dc: DataChannel) {}

        override fun onRenegotiationNeeded() {}

        override fun onAddTrack(receiver: RtpReceiver, mediaStreams: Array<MediaStream>) {}
    }

    // Implementation detail: handle offer creation/signaling and answer setting,
    // as well as adding remote ICE candidates once the answer SDP is set.
    private inner class SDPObserver : SdpObserver {
        override fun onCreateSuccess(origSdp: SessionDescription) {
            if (localSdp != null) {
                reportError("Multiple SDP create.")
                return
            }
            var sdpDescription = origSdp.description
            logi("onCreateSuccess: $sdpDescription")
            if (preferIsac) {
                sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC, true)
            }
            sdpDescription = preferCodec(
                sdpDescription,
                getSdpVideoCodecName(peerConnectionParameters!!), false
            )

            val sdp = SessionDescription(origSdp.type, sdpDescription)
            localSdp = sdp
            if (peerConnection != null && !isError) {
                logd("Set local SDP from " + origSdp.type)
                peerConnection!!.setLocalDescription(sdpObserver, localSdp)
            }
        }

        override fun onSetSuccess() {
            if (peerConnection == null || isError) {
                return
            }

            if (isInitiator) {
                // For offering peer connection we first create offer and set
                // local SDP, then after receiving answer set remote SDP.
                if (peerConnection!!.remoteDescription == null) {
                    // We've just set our local SDP so time to send it.
                    logd("Local SDP set succesfully")
                    events!!.onLocalDescriptionSet(localSdp!!)//fixSdpBundle(localSdp, true));
                } else {
                    // We've just set remote description, so drain remote
                    // and send local ICE candidates.
                    logd("Remote SDP set succesfully")
                    events!!.onRemoteDescriptionSet(remoteSdp!!)
                }
            } else {
                // For answering peer connection we set remote SDP and then
                // create answer and set local SDP.
                if (peerConnection!!.localDescription != null) {
                    // We've just set our local SDP so time to send it, drain
                    // remote and send local ICE candidates.
                    logd("Local SDP set succesfully")
                    events!!.onLocalDescriptionSet(localSdp!!)//fixSdpBundle(localSdp, true));
                } else {
                    // We've just set remote SDP - do nothing for now -
                    // answer will be created soon.
                    logd("Remote SDP set succesfully")
                    events!!.onRemoteDescriptionSet(remoteSdp!!)
                }
            }
        }

        override fun onCreateFailure(error: String) {
            reportError("createSDP error: $error")
        }

        override fun onSetFailure(error: String) {
            reportError("setSDP error: $error")
        }
    }

    companion object {
        private val VIDEO_TRACK_ID = "ARDAMSv0"
        private val AUDIO_TRACK_ID = "ARDAMSa0"
        private val VIDEO_TRACK_TYPE = "video"
        private val TAG = "PeerConnectionClient"
        private val VIDEO_CODEC_VP8 = "VP8"
        private val VIDEO_CODEC_VP9 = "VP9"
        private val VIDEO_CODEC_H264 = "H264"
        private val VIDEO_CODEC_H264_BASELINE = "H264 Baseline"
        private val VIDEO_CODEC_H264_HIGH = "H264 High"
        private val AUDIO_CODEC_OPUS = "opus"
        private val AUDIO_CODEC_ISAC = "ISAC"
        private val VIDEO_CODEC_PARAM_START_BITRATE = "x-google-start-bitrate"
        private val VIDEO_FLEXFEC_FIELDTRIAL = "WebRTC-FlexFEC-03-Advertised/Enabled/WebRTC-FlexFEC-03/Enabled/"
        private val VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL = "WebRTC-IntelVP8/Enabled/"
        private val DISABLE_WEBRTC_AGC_FIELDTRIAL = "WebRTC-Audio-MinimizeResamplingOnMobile/Enabled/"
        private val AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate"
        private val AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation"
        private val AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl"
        private val AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter"
        private val AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression"
        private val HD_VIDEO_WIDTH = 1280
        private val HD_VIDEO_HEIGHT = 720
        private val BPS_IN_KBPS = 1000

        private fun getSdpVideoCodecName(parameters: PeerConnectionParameters): String {
            when (parameters.videoCodec) {
                VIDEO_CODEC_VP8 -> return VIDEO_CODEC_VP8
                VIDEO_CODEC_VP9 -> return VIDEO_CODEC_VP9
                VIDEO_CODEC_H264_HIGH, VIDEO_CODEC_H264_BASELINE -> return VIDEO_CODEC_H264
                else -> return VIDEO_CODEC_VP8
            }
        }

        private fun getFieldTrials(peerConnectionParameters: PeerConnectionParameters): String {
            var fieldTrials = ""
            if (peerConnectionParameters.videoFlexfecEnabled) {
                fieldTrials += VIDEO_FLEXFEC_FIELDTRIAL
                logd("Enable FlexFEC field trial.")
            }
            fieldTrials += VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL
            if (peerConnectionParameters.disableWebRtcAGCAndHPF) {
                fieldTrials += DISABLE_WEBRTC_AGC_FIELDTRIAL
                logd("Disable WebRTC AGC field trial.")
            }
            return fieldTrials
        }

        private fun setStartBitrate(
            codec: String,
            isVideoCodec: Boolean,
            sdpDescription: String,
            bitrateKbps: Int
        ): String {
            val lines = sdpDescription.split("\r\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var rtpmapLineIndex = -1
            var sdpFormatUpdated = false
            var codecRtpMap: String? = null
            // Search for codec rtpmap in format
            // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
            var regex = "^a=rtpmap:(\\d+) $codec(/\\d+)+[\r]?$"
            var codecPattern = Pattern.compile(regex)
            for (i in lines.indices) {
                val codecMatcher = codecPattern.matcher(lines[i])
                if (codecMatcher.matches()) {
                    codecRtpMap = codecMatcher.group(1)
                    rtpmapLineIndex = i
                    break
                }
            }
            if (codecRtpMap == null) {
                logw("No rtpmap for $codec codec")
                return sdpDescription
            }
            logd("Found " + codec + " rtpmap " + codecRtpMap + " at " + lines[rtpmapLineIndex])
            // Check if a=fmtp string already exist in remote SDP for this codec and
            // update it with new bitrate parameter.
            regex = "^a=fmtp:$codecRtpMap \\w+=\\d+.*[\r]?$"
            codecPattern = Pattern.compile(regex)
            for (i in lines.indices) {
                val codecMatcher = codecPattern.matcher(lines[i])
                if (codecMatcher.matches()) {
                    logd("Found " + codec + " " + lines[i])
                    if (isVideoCodec) {
                        lines[i] += "; $VIDEO_CODEC_PARAM_START_BITRATE=$bitrateKbps"
                    } else {
                        lines[i] += "; " + AUDIO_CODEC_PARAM_BITRATE + "=" + bitrateKbps * 1000
                    }
                    logd("Update remote SDP line: " + lines[i])
                    sdpFormatUpdated = true
                    break
                }
            }
            val newSdpDescription = StringBuilder()
            for (i in lines.indices) {
                newSdpDescription.append(lines[i]).append("\r\n")
                // Append new a=fmtp line if no such line exist for a codec.
                if (!sdpFormatUpdated && i == rtpmapLineIndex) {
                    val bitrateSet: String = if (isVideoCodec) {
                        "a=fmtp:$codecRtpMap $VIDEO_CODEC_PARAM_START_BITRATE=$bitrateKbps"
                    } else {
                        ("a=fmtp:" + codecRtpMap + " " + AUDIO_CODEC_PARAM_BITRATE + "="
                                + bitrateKbps * 1000)
                    }
                    logd("Add remote SDP line: $bitrateSet")
                    newSdpDescription.append(bitrateSet).append("\r\n")
                }
            }
            return newSdpDescription.toString()
        }

        /**
         * Returns the line number containing "m=audio|video", or -1 if no such line exists.
         */
        private fun findMediaDescriptionLine(isAudio: Boolean, sdpLines: Array<String>): Int {
            val mediaDescription = if (isAudio) "m=audio " else "m=video "
            for (i in sdpLines.indices) {
                if (sdpLines[i].startsWith(mediaDescription)) {
                    return i
                }
            }
            return -1
        }

        private fun joinString(
            s: Iterable<CharSequence>,
            delimiter: String,
            delimiterAtEnd: Boolean
        ): String {

            val iter = s.iterator()
            if (!iter.hasNext()) {
                return ""
            }
            val buffer = StringBuilder(iter.next())
            while (iter.hasNext()) {
                buffer.append(delimiter).append(iter.next())
            }
            if (delimiterAtEnd) {
                buffer.append(delimiter)
            }
            return buffer.toString()
        }

        private fun movePayloadTypesToFront(
            preferredPayloadTypes: List<String>,
            mLine: String
        ): String? {

            // The format of the media description line should be: m=<media> <port> <proto> <fmt> ...
            val origLineParts = Arrays.asList(*mLine.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            if (origLineParts.size <= 3) {
                loge("Wrong SDP media description format: $mLine")
                return null
            }
            val header = origLineParts.subList(0, 3)
            val unpreferredPayloadTypes = ArrayList(origLineParts.subList(3, origLineParts.size))
            unpreferredPayloadTypes.removeAll(preferredPayloadTypes)
            // Reconstruct the line with |preferredPayloadTypes| moved to the beginning of the payload
            // types.
            val newLineParts = ArrayList<String>()
            newLineParts.addAll(header)
            newLineParts.addAll(preferredPayloadTypes)
            newLineParts.addAll(unpreferredPayloadTypes)
            return joinString(newLineParts, " ", false)
        }

        private fun preferCodec(
            sdpDescription: String,
            codec: String,
            isAudio: Boolean
        ): String {

            val lines = sdpDescription.split("\r\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val mLineIndex = findMediaDescriptionLine(isAudio, lines)
            if (mLineIndex == -1) {
                logw("No mediaDescription line, so can't prefer $codec")
                return sdpDescription
            }
            // A list with all the payload types with name |codec|. The payload types are integers in the
            // range 96-127, but they are stored as strings here.
            val codecPayloadTypes = ArrayList<String>()
            // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
            val codecPattern = Pattern.compile("^a=rtpmap:(\\d+) $codec(/\\d+)+[\r]?$")
            for (line in lines) {
                val codecMatcher = codecPattern.matcher(line)
                if (codecMatcher.matches()) {
                    codecPayloadTypes.add(codecMatcher.group(1))
                }
            }
            if (codecPayloadTypes.isEmpty()) {
                logw("No payload types with name $codec")
                return sdpDescription
            }
            val newMLine = movePayloadTypesToFront(codecPayloadTypes, lines[mLineIndex]) ?: return sdpDescription
            logd("Change media description from: " + lines[mLineIndex] + " to " + newMLine)
            lines[mLineIndex] = newMLine
            return joinString(Arrays.asList(*lines), "\r\n", true)
        }
    }
}
