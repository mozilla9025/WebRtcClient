package app.rtcmeetings.webrtc


import android.content.Context
import app.rtcmeetings.util.logd
import app.rtcmeetings.util.loge
import app.rtcmeetings.util.logi
import app.rtcmeetings.webrtc.video.CamCapturer
import app.rtcmeetings.webrtc.video.CamSide
import org.webrtc.*
import org.webrtc.PeerConnection
import org.webrtc.SurfaceTextureHelper
import org.webrtc.audio.AudioDeviceModule
import org.webrtc.audio.JavaAudioDeviceModule

class PeerConnectionClient private constructor(
    private val appContext: Context,
    private val rootEglBase: EglBase,
    private val localVideoSink: VideoSink,
    private val remoteVideoSink: VideoSink
) {

    var webRtcClientListener: WebRtcClientListener? = null
    var localVideoEnabled: Boolean = true

    private var localAudioEnabled: Boolean = true
    private var remoteVideoEnabled: Boolean = true

    private var audioConstraints: MediaConstraints? = null
    private var sdpConstraints: MediaConstraints? = null

    private lateinit var videoCapturer: CamCapturer
    private lateinit var factory: PeerConnectionFactory

    private lateinit var videoSource: VideoSource
    private lateinit var audioSource: AudioSource

    private var peerConnection: PeerConnection? = null

    private var localSdp: SessionDescription? = null

    private val candidates: MutableList<IceCandidate> = ArrayList()

    private var initiator = false

    private var localAudioTrack: AudioTrack? = null

    private var localVideoTrack: VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var localVideoSender: RtpSender? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private var remoteSdpSat = false

    private val javaAudioDeviceModule: AudioDeviceModule =
        JavaAudioDeviceModule.builder(appContext)
            .createAudioDeviceModule()

    private val pcObserver = object : PeerConnection.Observer {

        override fun onIceCandidate(iceCandidate: IceCandidate?) {
            webRtcClientListener?.onIceCandidate(iceCandidate!!)
        }

        override fun onDataChannel(dataChannel: DataChannel?) {
        }

        override fun onIceConnectionReceivingChange(p0: Boolean) {
        }

        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
            when (p0) {
                PeerConnection.IceConnectionState.CONNECTED -> {
                    webRtcClientListener?.onConnected()
                }
                PeerConnection.IceConnectionState.DISCONNECTED -> {
                    webRtcClientListener?.onDisconnected()
                }
                PeerConnection.IceConnectionState.FAILED -> {
                    webRtcClientListener?.onFailed()
                }
                else -> {
                    logi("$p0")
                }
            }
        }

        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        }

        override fun onAddStream(mediaStream: MediaStream) {
        }

        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        }

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        }

        override fun onRemoveStream(p0: MediaStream?) {
        }

        override fun onRenegotiationNeeded() {
        }

        override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        }
    }

    init {
        createMediaConstraints()
        createPeerConnectionFactory()
        createPeerConnection()
    }

    fun createOffer() {
        initiator = true
        peerConnection?.createOffer(sdpObserver, sdpConstraints)
    }

    fun createAnswer() {
        initiator = false
        peerConnection?.createAnswer(sdpObserver, sdpConstraints)
    }

    fun addIceCandidate(iceCandidate: IceCandidate) {
        if (remoteSdpSat)
            peerConnection?.addIceCandidate(iceCandidate)
        else
            candidates.add(iceCandidate)
    }

    fun setRemoteSdp(sessionDescription: SessionDescription) {
        if (remoteSdpSat) return

        peerConnection?.setRemoteDescription(sdpObserver, sessionDescription)
    }

    fun toggleMic(): Boolean {
        localAudioTrack?.let {
            localAudioEnabled = !localAudioEnabled
            it.setEnabled(localAudioEnabled)
        }
        return localAudioEnabled
    }

    fun toggleCam(): Boolean {
        localVideoTrack?.let {
            localVideoEnabled = !localVideoEnabled
            it.setEnabled(localVideoEnabled)
        }
        return localVideoEnabled
    }

    fun toggleRemoteVideo(isEnabled: Boolean) {
        remoteVideoTrack?.let {
            remoteVideoEnabled = isEnabled
            it.setEnabled(remoteVideoEnabled)
        }
    }

    fun switchCamera(): CamSide {
        videoCapturer.switchCamera()
        return videoCapturer.cameraSide
    }

    fun getCameraSide(): CamSide = videoCapturer.cameraSide

    private fun createPeerConnection() {

        val iceServers = listOf(
            PeerConnection.IceServer
                .builder("turn:turn.connectycube.com:5349?transport=udp")
                .setUsername("connectycube")
                .setPassword("4c29501ca9207b7fb9c4b4b6b04faeb1")
                .setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK)
                .createIceServer(),
            PeerConnection.IceServer
                .builder("turn:turn.connectycube.com:5349?transport=tcp")
                .setUsername("connectycube")
                .setPassword("4c29501ca9207b7fb9c4b4b6b04faeb1")
                .setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK)
                .createIceServer(),
            PeerConnection.IceServer
                .builder("stun:turn.connectycube.com")
                .setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK)
                .createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            candidateNetworkPolicy = PeerConnection.CandidateNetworkPolicy.ALL
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            keyType = PeerConnection.KeyType.ECDSA
            enableDtlsSrtp = true
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        peerConnection = factory.createPeerConnection(rtcConfig, pcObserver)

        val mediaStreamLabels = listOf("ARDAMS")

        videoCapturer = CamCapturer.create(appContext)

        localVideoTrack = createVideoTrack(videoCapturer.capturer)
        localAudioTrack = createAudioTrack()

        peerConnection?.run {
            addTrack(localVideoTrack, mediaStreamLabels)
            addTrack(localAudioTrack, mediaStreamLabels)
        }
        remoteVideoTrack = getRemoteVideoTrack()
        remoteVideoTrack?.run {
            setEnabled(remoteVideoEnabled)
            addSink(remoteVideoSink)
        }

        findVideoSender()

        logi("Peer connection created.")
    }

    private fun createMediaConstraints() {
        audioConstraints = MediaConstraints()
        sdpConstraints = MediaConstraints()

        sdpConstraints!!.mandatory.run {
            add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        sdpConstraints!!.optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
        audioConstraints!!.optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
    }

    private fun createPeerConnectionFactory() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(appContext)
                .createInitializationOptions()
        )

        val encoderFactory = DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)

        factory = PeerConnectionFactory.builder().apply {
            setOptions(PeerConnectionFactory.Options())
            setAudioDeviceModule(javaAudioDeviceModule)
            setVideoEncoderFactory(encoderFactory)
            setVideoDecoderFactory(decoderFactory)
        }.createPeerConnectionFactory()

        logi("Peer connection factory created.")
        javaAudioDeviceModule.release()
    }

    private fun createAudioTrack(): AudioTrack {
        audioSource = factory.createAudioSource(audioConstraints)
        val audioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource)
        audioTrack.setEnabled(localAudioEnabled)
        return audioTrack
    }

    private fun createVideoTrack(capturer: VideoCapturer): VideoTrack {
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.eglBaseContext)
        videoSource = factory.createVideoSource(capturer.isScreencast)
        capturer.initialize(surfaceTextureHelper, appContext, videoSource.capturerObserver)
        capturer.startCapture(HD_VIDEO_WIDTH, HD_VIDEO_HEIGHT, DEFAULT_FPS)
        val videoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource)
        videoTrack.setEnabled(localVideoEnabled)
        videoTrack.addSink(localVideoSink)
        return videoTrack
    }

    private fun findVideoSender() {
        peerConnection?.senders!!.forEach { sender ->
            if (sender.track() != null) {
                val trackType = sender!!.track()?.kind()
                if (trackType == VIDEO_TRACK_TYPE) {
                    logi("Find video sender!")
                    localVideoSender = sender
                }
            }
        }
    }

    // Returns the remote VideoTrack, assuming there is only one.
    private fun getRemoteVideoTrack(): VideoTrack? {
        peerConnection?.transceivers!!.forEach { transceiver ->
            val track = transceiver.receiver.track()
            if (track is VideoTrack)
                return track
        }
        return null
    }

    private fun setReceivedCandidates() {
        if (peerConnection == null) return

        synchronized(candidates) {
            candidates.forEach {
                peerConnection!!.addIceCandidate(it)
            }
            candidates.clear()
        }
    }

    private val sdpObserver = object : SdpObserver {
        override fun onSetFailure(p0: String?) {
            logd("Set failure -> $p0")
        }

        override fun onSetSuccess() {
            if (peerConnection == null) {
                return
            }

            if (initiator) {
                if (peerConnection!!.remoteDescription == null) {
                    if (localSdp?.type == SessionDescription.Type.OFFER)
                        webRtcClientListener?.onOfferGenerated(localSdp!!)
                    else if (localSdp?.type == SessionDescription.Type.ANSWER)
                        webRtcClientListener?.onAnswerGenerated(localSdp!!)
                }
            } else if (peerConnection!!.localDescription != null) {
                if (localSdp?.type == SessionDescription.Type.OFFER)
                    webRtcClientListener?.onOfferGenerated(localSdp!!)
                else if (localSdp?.type == SessionDescription.Type.ANSWER)
                    webRtcClientListener?.onAnswerGenerated(localSdp!!)
            }

            if (peerConnection!!.remoteDescription != null) {
                remoteSdpSat = true
                setReceivedCandidates()
            }
        }

        override fun onCreateSuccess(description: SessionDescription) {
            localSdp = description
            peerConnection?.setLocalDescription(this, description)
        }

        override fun onCreateFailure(p0: String?) {}
    }

    fun cleanUp() {
        candidates.clear()
        peerConnection?.dispose()
        try {
            videoCapturer.stopCapture()
            videoCapturer.dispose()
        } catch (e: InterruptedException) {
            loge(e)
        }

        videoSource.dispose()
        surfaceTextureHelper?.dispose()
        factory.dispose()
        instance = null
    }

    companion object {
        private const val VIDEO_TRACK_ID = "ARDAMSv0"
        private const val AUDIO_TRACK_ID = "ARDAMSa0"
        private const val VIDEO_TRACK_TYPE = "video"
        private const val HD_VIDEO_WIDTH = 720
        private const val HD_VIDEO_HEIGHT = 1280
        private const val DEFAULT_FPS = 30

        private var instance: PeerConnectionClient? = null

        fun getInstance(
            context: Context,
            eglBase: EglBase,
            localVideoSink: VideoSink,
            remoteVideoSink: VideoSink
        ): PeerConnectionClient {
            if (instance == null)
                instance = PeerConnectionClient(context, eglBase, localVideoSink, remoteVideoSink)

            return instance!!
        }
    }
}