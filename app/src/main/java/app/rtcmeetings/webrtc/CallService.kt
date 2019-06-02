package app.rtcmeetings.webrtc

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import app.rtcmeetings.R
import app.rtcmeetings.domain.usecase.CallUseCase
import app.rtcmeetings.network.WsService
import app.rtcmeetings.network.request.CallRequest
import app.rtcmeetings.ui.CallActivity
import app.rtcmeetings.webrtc.audio.CallAudioManager
import app.rtcmeetings.webrtc.video.CamSide
import app.rtcmeetings.webrtc.video.ProxyVideoSink
import com.google.gson.GsonBuilder
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import org.webrtc.VideoSink
import java.util.concurrent.Executors
import javax.inject.Inject

class CallService : Service(), WebRtcClientListener {

    private var startTime: Long? = null
    @Inject
    lateinit var callUseCase: CallUseCase


    lateinit var eglBase: EglBase
    var callState: CallState? = null
    var cameraSide: CamSide? = null

    //default values
    var localVideoEnabled = false
    var remoteVideoEnabled = false
    var localMicEnabled = true
    var isLoopEnabled = false

    private var webRtcClient: PeerConnectionClient? = null

    private val executor = Executors.newSingleThreadExecutor()
    private val disposables = CompositeDisposable()
    private val gson = GsonBuilder().create()

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
    private lateinit var remoteOffer: CallRequest

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

    //TODO remove this function & replace calls by NotificationManager
    private fun getNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("qwe", "qwerty", NotificationManager.IMPORTANCE_DEFAULT)
            // The PendingIntent to launch our activity if the user selects this notification

            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)

            val contentIntent = PendingIntent.getActivity(
                this, 0,
                Intent(this, CallActivity::class.java), 0
            )
            // Set the info for the views that show in the notification panel.
            return Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_accept_call)  // the status icon
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setChannelId(channel.id)
                .setContentTitle(getText(R.string.app_name))
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build()
        } else {
            val contentIntent = PendingIntent.getActivity(
                this, 0,
                Intent(this, CallActivity::class.java), 0
            )
            // Set the info for the views that show in the notification panel.
            return Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_accept_call)  // the status icon
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.app_name))
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        executor.execute {
            when (intent?.action) {
                ACTION_START_OUTGOING_CALL -> handleStartOutgoingCall(intent)
                ACTION_START_INCOMING_CALL -> handleStartIncomingCall(intent)
                ACTION_ACCEPT_INCOMING_CALL -> handleAcceptIncomingCall()
                ACTION_DECLINE_INCOMING_CALL -> handleDeclineIncomingCall()
                ACTION_CANCEL_OUTGOING_CALL -> handleCancelOutgoingCall()
                ACTION_LOCAL_SWITCH_CAMERA -> handleLocalSwitchCamera()
                ACTION_LOCAL_TOGGLE_CAMERA -> handleLocalToggleCamera()
                ACTION_LOCAL_TOGGLE_MICROPHONE -> handleLocalToggleMicrophone()
                ACTION_STOP_INCOMING_RINGER -> handleStopIncomingRinger()
                ACTION_FINISH_CALL -> handleFinishCall()
                ACTION_TERMINATE -> handleTerminate()
            }
        }

        return START_NOT_STICKY
    }

    override fun onIceCandidate(iceCandidate: IceCandidate) {
    }

    override fun onAnswerGenerated(sdp: SessionDescription) {
        answerSdpSat = true
    }

    override fun onOfferGenerated(sdp: SessionDescription) {
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
        callEventListener?.onDisconnect()
        ServiceActionHelper.terminate(this@CallService)
    }

    override fun onFailed() {
        callEventListener?.onFail()
        ServiceActionHelper.terminate(this@CallService)
    }

    fun setCallEventListener(listener: CallEventListener?) {
        callEventListener = listener
    }

    fun setTargets(localTarget: VideoSink, remoteTarget: VideoSink) {
        localVideoSinkProxy.target = localTarget
        remoteVideoSinkProxy.target = remoteTarget
    }


    private fun onIncomingCallReady() {
        audioManager.startIncomingRinger(true)
        CallActivity.start(this@CallService)
    }

    private fun onOutgoingCallReady() {
        initWebRtc()
        webRtcClient?.createOffer()
    }

    private fun handleStartIncomingCall(intent: Intent) {
        if (phoneState == PhoneState.BUSY) return

        phoneState = PhoneState.BUSY
        callState = CallState.INCOMING_PENDING


        onIncomingCallReady()

    }

    private fun handleStartOutgoingCall(intent: Intent) {
        if (phoneState == PhoneState.BUSY) return

        phoneState = PhoneState.BUSY
        callState = CallState.OUTGOING_PENDING

        onOutgoingCallReady()
        CallActivity.start(this@CallService)
    }

    private fun handleAcceptIncomingCall() {
        initWebRtc()
        webRtcClient?.createAnswer()
    }

    private fun handleDeclineIncomingCall() {
        audioManager.stopIncomingRinger()
        ServiceActionHelper.terminate(this)
    }

    private fun handleCancelOutgoingCall() {
        ServiceActionHelper.terminate(this)
    }

    private fun handleFinishCall() {
        ServiceActionHelper.terminate(this)
    }

    private fun handleTerminate() {
        startForeground(234234, getNotification())

        disposables.clear()
        startTime = null
        webRtcClient?.cleanUp()
        audioManager.stop(false)
        stopForeground(true)
        stopSelf()
    }

    private fun initWebRtc() {
        webRtcClient = PeerConnectionClient.getInstance(this, eglBase, localVideoSinkProxy, remoteVideoSinkProxy)
        webRtcClient!!.webRtcClientListener = this
    }


    private fun handleLocalSwitchCamera() {
        val side = webRtcClient?.switchCamera()
        side?.let {
            cameraSide = it
            deviceEventListener?.onCamSwitch(it)
        }
    }

    private fun handleLocalToggleCamera() {

    }

    private fun handleLocalToggleMicrophone() {
        localMicEnabled = webRtcClient?.toggleMic()!!
        deviceEventListener?.onMicToggle(localMicEnabled)
    }

    private fun handleStopIncomingRinger() {
        audioManager.stopIncomingRinger()
    }


    private fun addLocalCandidate(iceCandidate: IceCandidate) {
        iceCandidateList.add(iceCandidate)
    }

    internal enum class PhoneState {
        IDLE, BUSY
    }

    inner class LocalBinder : Binder() {
        internal val service: CallService
            get() = this@CallService
    }

    companion object {
        internal const val ACTION_START_OUTGOING_CALL = "action_start_outgoing_call"
        internal const val ACTION_START_INCOMING_CALL = "action_start_incoming_call"
        internal const val ACTION_ACCEPT_INCOMING_CALL = "action_accept_incoming_call"
        internal const val ACTION_DECLINE_INCOMING_CALL = "action_decline_incoming_call"
        internal const val ACTION_CANCEL_OUTGOING_CALL = "action_cancel_outgoing_call"
        internal const val ACTION_FINISH_CALL = "action_finish_active_call"

        internal const val ACTION_LOCAL_SWITCH_CAMERA = "action_local_switch_camera"
        internal const val ACTION_LOCAL_TOGGLE_CAMERA = "action_local_toggle_camera"
        internal const val ACTION_LOCAL_TOGGLE_MICROPHONE = "action_local_toggle_microphone"
        internal const val ACTION_STOP_INCOMING_RINGER = "action_stop_incoming_ringer"

        internal const val ACTION_TERMINATE = "action_terminate"

        internal const val EXTRA_STRING = "call_extra_string_value"
    }
}