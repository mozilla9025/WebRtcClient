package app.rtcmeetings.webrtc

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import app.rtcmeetings.R
import app.rtcmeetings.data.entity.User
import app.rtcmeetings.domain.usecase.CallUseCase
import app.rtcmeetings.network.request.CallRequest
import app.rtcmeetings.network.request.IceExchange
import app.rtcmeetings.network.ws.WsEvent
import app.rtcmeetings.network.ws.WsService
import app.rtcmeetings.ui.CallActivity
import app.rtcmeetings.webrtc.audio.CallAudioManager
import app.rtcmeetings.webrtc.video.CamSide
import app.rtcmeetings.webrtc.video.ProxyVideoSink
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import org.webrtc.VideoSink
import java.util.concurrent.Executors
import javax.inject.Inject

class CallService : Service(), WebRtcClientListener {

    @Inject
    lateinit var callUseCase: CallUseCase
    @Inject
    lateinit var gson: Gson

    lateinit var eglBase: EglBase
    var callState: CallState? = null
    var cameraSide: CamSide? = null
    var interlocutor: User? = null

    private var startTime: Long? = null
    private var socketId: String? = null
    private var callId: Int? = null
    private var remoteOffer: String? = null

    private var finishing = false

    private var webRtcClient: PeerConnectionClient? = null

    private val executor = Executors.newSingleThreadExecutor()
    private val disposables = CompositeDisposable()

    private val localVideoSinkProxy = ProxyVideoSink()
    private val remoteVideoSinkProxy = ProxyVideoSink()

    private var phoneState: PhoneState = PhoneState.IDLE

    private var callEventListener: CallEventListener? = null
    private var deviceEventListener: DeviceEventListener? = null

    private val binder = LocalBinder()
    private val iceCandidateList: ArrayList<IceCandidate> = arrayListOf()

    private var connected = false
    private var answerSdpSat = false

    private lateinit var audioManager: CallAudioManager

    fun setCallEventListener(listener: CallEventListener?) {
        callEventListener = listener
    }

    fun setDeviceEventListener(listener: DeviceEventListener?) {
        deviceEventListener = listener
    }

    fun setTargets(localTarget: VideoSink, remoteTarget: VideoSink) {
        localVideoSinkProxy.target = localTarget
        remoteVideoSinkProxy.target = remoteTarget
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
        eglBase = EglBase.create()

        audioManager = CallAudioManager(this)
        audioManager.initializeAudioForCall()

        startForeground(234234, getNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        executor.execute {
            when (intent?.action) {
                ACTION_OUTGOING_CALL -> handleStartOutgoingCall(intent)
                ACTION_INCOMING_CALL -> handleStartIncomingCall(intent)

                ACTION_LOCAL_ACCEPT -> handleAcceptIncomingCall()
                ACTION_LOCAL_DECLINE -> handleDeclineIncomingCall()
                ACTION_LOCAL_CANCEL -> handleCancelOutgoingCall()
                ACTION_LOCAL_FINISH -> handleFinishCall()

                ACTION_STOP_INCOMING_RINGER -> handleStopIncomingRinger()

                ACTION_LOCAL_VIDEO_TOGGLE -> handleLocalToggleCamera()
                ACTION_LOCAL_SWITCH_CAMERA -> handleLocalSwitchCamera()
                ACTION_LOCAL_TOGGLE_MICROPHONE -> handleLocalToggleMicrophone()

                ACTION_REMOTE_ACCEPTED -> handleRemoteAccept(intent)
                ACTION_REMOTE_DECLINE -> handleRemoteDecline(intent)
                ACTION_REMOTE_CANCEL -> handleRemoteCancel(intent)
                ACTION_REMOTE_FINISH -> handleRemoteFinish(intent)
                ACTION_REMOTE_ICE -> handleRemoteIceCandidate(intent)
                ACTION_REMOTE_VIDEO_TOGGLE -> handleRemoteVideoToggle(intent)

                ACTION_TERMINATE -> handleTerminate()
            }
        }

        return START_NOT_STICKY
    }

    override fun onIceCandidate(candidate: IceCandidate) {
        if (callState != CallState.CONNECTED)
            addLocalCandidate(candidate)
        else
            sendIceCandidate(candidate)
    }

    override fun onAnswerGenerated(sdp: SessionDescription) {
        answerSdpSat = true
        disposables.add(
                callUseCase.acceptCall(callId!!, sdp.description, socketId!!)
                        .subscribe({
                            callState = CallState.INCOMING_CONNECTING
                            callEventListener?.onCreatingConnection()
                            sendLocalIceCandidates()
                        }, {
                            callState = CallState.FAILED
                            callEventListener?.onFail()
                            clearAndFinish()
                        })
        )
    }

    override fun onOfferGenerated(sdp: SessionDescription) {
        disposables.add(
                callUseCase.startCall(
                        socketId!!,
                        sdp.description,
                        interlocutor?.id!!
                ).subscribe({ id ->
                    callId = id
                    callState = CallState.OUTGOING_CONNECTING
                    callEventListener?.onCreatingConnection()
                }, {
                    callState = CallState.FAILED
                    callEventListener?.onFail()
                    CallEvent.terminate(this@CallService)
                })
        )
    }

    override fun onConnected() {
        connected = true
        startTime = System.currentTimeMillis()
        callState = CallState.CONNECTED
        cameraSide = webRtcClient?.getCameraSide()
        audioManager.startCommunication(true)
        callEventListener?.onConnect()
    }

    override fun onDisconnected() {
        if (finishing) return
        callEventListener?.onDisconnect()
        clearAndFinish()
    }

    override fun onFailed() {
        callEventListener?.onFail()
        CallEvent.terminate(this@CallService)
    }

    private fun handleStartIncomingCall(intent: Intent) {
        if (phoneState == PhoneState.BUSY) return

        socketId = intent.getStringExtra(EXTRA_SOCKET_ID)
        val extras = intent.getStringExtra(EXTRA_STRING)
        val json = gson.fromJson(extras, JsonObject::class.java)

        interlocutor = gson.fromJson<User>(json.get("account").asJsonObject, User::class.java)
        remoteOffer = json.get("initSDP").asString
        callId = json.get("id").asInt

        phoneState = PhoneState.BUSY
        callState = CallState.INCOMING_PENDING
        audioManager.startIncomingRinger(true)
        CallActivity.start(this@CallService)
    }

    private fun handleStartOutgoingCall(intent: Intent) {
        if (phoneState == PhoneState.BUSY) return

        phoneState = PhoneState.BUSY
        callState = CallState.OUTGOING_PENDING
        interlocutor = intent.getParcelableExtra(EXTRA_USER)
        socketId = intent.getStringExtra(EXTRA_STRING)

        initWebRtc()
        webRtcClient?.createOffer()
        CallActivity.start(this@CallService)
    }

    private fun handleAcceptIncomingCall() {
        initWebRtc()
        webRtcClient?.setRemoteSdp(SessionDescription(SessionDescription.Type.OFFER, remoteOffer))
        webRtcClient?.createAnswer()
    }

    private fun handleDeclineIncomingCall() {
        audioManager.stopIncomingRinger()
        callId?.let {
            disposables.add(callUseCase.declineCall(it)
                    .subscribe({ clearAndFinish() },
                            { clearAndFinish() }))
        } ?: clearAndFinish()
    }

    private fun handleCancelOutgoingCall() {
        callId?.let {
            disposables.add(callUseCase.cancelCall(it)
                    .subscribe({ clearAndFinish() },
                            { clearAndFinish() }))
        } ?: clearAndFinish()
    }

    private fun handleFinishCall() {
        callId?.let {
            disposables.add(callUseCase.finishCall(it)
                    .subscribe({ clearAndFinish() },
                            { clearAndFinish() })
            )
        } ?: clearAndFinish()
    }

    private fun handleRemoteAccept(intent: Intent) {
        if (callId == null) {
            callEventListener?.onFail()
            clearAndFinish()
        }

        val extras = intent.getStringExtra(EXTRA_STRING)
        val json = gson.fromJson(extras, JsonObject::class.java)
        val id = json.get("id").asInt
        val sdp = json.get("targetSDP").asString

        if (id == callId) {
            webRtcClient?.setRemoteSdp(SessionDescription(SessionDescription.Type.ANSWER, sdp))
            sendLocalIceCandidates()
        }
    }

    private fun handleRemoteDecline(intent: Intent) {
        val extras = intent.getStringExtra(EXTRA_STRING)

        val id = gson.fromJson(extras, JsonObject::class.java).get("id").asInt

        if (callId == id || callId == null) {
            callEventListener?.onFinish()
            clearAndFinish()
        }
    }

    private fun handleRemoteCancel(intent: Intent) {
        val extras = intent.getStringExtra(EXTRA_STRING)

        val id = gson.fromJson(extras, JsonObject::class.java).get("id").asInt

        if (callId == id || callId == null) {
            callEventListener?.onFinish()
            clearAndFinish()
        }
    }

    private fun handleRemoteFinish(intent: Intent) {
        val extras = intent.getStringExtra(EXTRA_STRING)

        val id = gson.fromJson(extras, JsonObject::class.java).get("id").asInt

        if (callId == id || callId == null) {
            callEventListener?.onFinish()
            clearAndFinish()
        }
    }

    private fun handleRemoteIceCandidate(intent: Intent) {
        val extras = intent.getStringExtra(EXTRA_STRING)
        val exchange = gson.fromJson<IceExchange>(extras, IceExchange::class.java)
        val candidate = IceCandidate(exchange.sdpMid, exchange.mLineIndex, exchange.sdp)
        webRtcClient?.addIceCandidate(candidate)
    }

    private fun handleRemoteVideoToggle(intent: Intent) {
        val extras = intent.getStringExtra(EXTRA_STRING)
        val enabled = gson.fromJson<Boolean>(extras, Boolean::class.java)
        if (enabled)
            callEventListener?.onRemoteUserStartStream()
        else
            callEventListener?.onRemoteUserStopStream()
    }

    private fun handleTerminate() {
        startForeground(234234, getNotification())

        disposables.clear()
        startTime = null
        iceCandidateList.clear()
        webRtcClient?.cleanUp()
        webRtcClient = null
        audioManager.stop(false)
        stopForeground(true)
        stopSelf()
    }

    private fun clearAndFinish() {
        if (finishing) return

        finishing = true
        disposables.clear()
        CallEvent.terminate(this@CallService)
    }

    private fun sendIceCandidate(iceCandidate: IceCandidate) {
        if (interlocutor == null) {
            callEventListener?.onFail()
            clearAndFinish()
            return
        }

        val callRequest = CallRequest().apply {
            userId = interlocutor!!.id
            event = WsEvent.ICE_EXCHANGE
            payload = gson.toJson(IceExchange(
                    iceCandidate.sdpMid,
                    iceCandidate.sdpMLineIndex,
                    iceCandidate.sdp
            ))
        }
        WsService.emit(this@CallService, gson.toJson(callRequest))
    }

    private fun sendLocalIceCandidates() {
        iceCandidateList.forEach { sendIceCandidate(it) }
        iceCandidateList.clear()
    }

    private fun initWebRtc() {
        webRtcClient = PeerConnectionClient.getInstance(
                applicationContext,
                eglBase,
                localVideoSinkProxy,
                remoteVideoSinkProxy
        )
        webRtcClient!!.webRtcClientListener = this
    }

    private fun handleLocalSwitchCamera() {
        webRtcClient?.switchCamera()?.let {
            cameraSide = it
            deviceEventListener?.onCamSwitch(it)
        }
    }

    private fun handleLocalToggleCamera() {
        webRtcClient?.toggleCam()?.let {
            deviceEventListener?.onCamToggle(it)
            val callRequest = CallRequest().apply {
                userId = interlocutor!!.id
                event = WsEvent.VIDEO_TOGGLE
                payload = gson.toJson(it)
            }
            WsService.emit(this@CallService, gson.toJson(callRequest))
        }
    }

    private fun handleLocalToggleMicrophone() {
        webRtcClient?.toggleMic()?.let {
            deviceEventListener?.onMicToggle(it)
        }
    }

    private fun handleStopIncomingRinger() {
        audioManager.stopIncomingRinger()
    }

    private fun addLocalCandidate(iceCandidate: IceCandidate) {
        iceCandidateList.add(iceCandidate)
    }

    private fun getNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("call", "WebRtcCall", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply {
                        importance = NotificationManager.IMPORTANCE_DEFAULT
                        enableVibration(false)
                    }

            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)

            val contentIntent = PendingIntent.getActivity(
                    this, 0,
                    Intent(this, CallActivity::class.java), 0
            )
            return Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_accept_call)
                    .setWhen(System.currentTimeMillis())
                    .setChannelId(channel.id)
                    .setContentTitle(getText(R.string.app_name))
                    .setContentIntent(contentIntent)
                    .build()
        } else {
            val contentIntent = PendingIntent.getActivity(
                    this, 0,
                    Intent(this, CallActivity::class.java), 0
            )
            return Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_accept_call)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(getText(R.string.app_name))
                    .setContentIntent(contentIntent)
                    .build()
        }
    }

    internal enum class PhoneState {
        IDLE, BUSY
    }

    inner class LocalBinder : Binder() {
        internal val service: CallService
            get() = this@CallService
    }

    companion object {
        const val ACTION_OUTGOING_CALL = "action_outgoing_call"
        const val ACTION_INCOMING_CALL = "action_incoming_call"

        const val ACTION_LOCAL_ACCEPT = "action_accept_incoming_call"
        const val ACTION_LOCAL_DECLINE = "action_decline_incoming_call"
        const val ACTION_LOCAL_CANCEL = "action_cancel_outgoing_call"
        const val ACTION_LOCAL_FINISH = "action_finish_active_call"
        const val ACTION_LOCAL_VIDEO_TOGGLE = "action_video_toggle"
        const val ACTION_STOP_INCOMING_RINGER = "action_stop_incoming_ringer"
        const val ACTION_LOCAL_SWITCH_CAMERA = "action_local_switch_camera"
        const val ACTION_LOCAL_TOGGLE_MICROPHONE = "action_local_toggle_microphone"

        const val ACTION_REMOTE_ACCEPTED = "action_remote_call_accept"
        const val ACTION_REMOTE_DECLINE = "action_remote_call_decline"
        const val ACTION_REMOTE_CANCEL = "action_remote_call_cancel"
        const val ACTION_REMOTE_FINISH = "action_remote_call_finish"
        const val ACTION_REMOTE_ICE = "action_remote_ice_candidate"
        const val ACTION_REMOTE_VIDEO_TOGGLE = "action_remote_video_toggle"

        const val ACTION_TERMINATE = "action_terminate"

        const val EXTRA_STRING = "call_extra_string_value"
        const val EXTRA_SOCKET_ID = "call_extra_socket_id"
        const val EXTRA_USER = "call_extra_user"
    }
}