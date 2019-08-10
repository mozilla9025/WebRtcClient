package app.rtcmeetings.ui.module

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.view.WindowManager
import app.rtcmeetings.R
import app.rtcmeetings.base.BaseActivity
import app.rtcmeetings.data.entity.User
import app.rtcmeetings.util.logw
import app.rtcmeetings.util.rxbus.RxBus
import app.rtcmeetings.util.rxbus.RxEvent
import app.rtcmeetings.webrtc.*
import app.rtcmeetings.webrtc.video.CamSide
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.include_active_call.*
import kotlinx.android.synthetic.main.include_incoming_call.*
import kotlinx.android.synthetic.main.include_outgoing_call.*

class CallActivity : BaseActivity(), CallEventListener, DeviceEventListener {

    private var callService: CallService? = null
    private var rxBusDisposable: Disposable? = null

    private var accepted = false
    private var denied = false

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            callService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            callService = (service as CallService.LocalBinder).service
            with(callService!!) {
                localVideoRenderer.run {
                    init(eglBase.eglBaseContext, null)
                    setMirror(true)
                }
                remoteVideoRenderer.run {
                    init(eglBase.eglBaseContext, null)
                }
                setTargets(localVideoRenderer, remoteVideoRenderer)
                setCallEventListener(this@CallActivity)
                setDeviceEventListener(this@CallActivity)

                when (callState) {
                    CallState.OUTGOING_PENDING, CallState.OUTGOING_CONNECTING -> setUpOutgoingCallView(interlocutor!!)
                    CallState.INCOMING_PENDING, CallState.INCOMING_CONNECTING -> setUpIncomingCallView(interlocutor!!)
                    CallState.CONNECTED -> setUpActiveCallView()
                    else -> {
                        logw("Call state is different than handled")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.run {
            addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        rxBusDisposable = RxBus.listen(RxEvent.CallFinish::class.java).subscribe {
            releaseVideoViews()
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        bindCallService()
        setClickListeners()
    }

    override fun onResume() {
        super.onResume()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        window.run {
            decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LOW_PROFILE
                        or View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

            addFlags(WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = 0
            navigationBarColor = 0
            addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION or
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
        }
    }

    override fun onStop() {
        super.onStop()
        callService?.run {
            setTargets(null, null)
            setDeviceEventListener(null)
            setCallEventListener(null)
        }
        releaseVideoViews()
        unbindCallService()
    }

    override fun onDestroy() {
        super.onDestroy()
        rxBusDisposable?.dispose()
        rxBusDisposable = null
    }

    override fun onConnect() {
        runOnUiThread {
            showToast("Connected")
            setUpActiveCallView()
        }
    }

    override fun onCreatingConnection() {
        runOnUiThread {
            showToast("Creating connection")
        }
    }

    override fun onDisconnect() {
        runOnUiThread {
            releaseVideoViews()
            finish()
        }
    }

    override fun onFail() {
        runOnUiThread {
            showToast("Failed to connect")
            releaseVideoViews()
            finish()
        }
    }

    override fun onRemoteUserStopStream() {
        runOnUiThread {
            viewBackground?.visibility = VISIBLE
        }
    }

    override fun onRemoteUserStartStream() {
        runOnUiThread {
            viewBackground?.visibility = GONE
        }
    }

    override fun onFinish() {
        runOnUiThread {
            releaseVideoViews()
            finish()
        }
    }

    override fun onCamToggle(isEnabled: Boolean) {
        runOnUiThread {
            if (isEnabled) {
                localVideoRenderer?.visibility = VISIBLE
            } else {
                localVideoRenderer?.visibility = GONE
            }
            btnCam?.setImageResource(if (isEnabled) R.drawable.ic_cam else R.drawable.ic_cam_off)
        }
    }

    override fun onMicToggle(isEnabled: Boolean) {
        runOnUiThread {
            btnMic?.setImageResource(if (isEnabled) R.drawable.ic_mic else R.drawable.ic_mic_off)
        }
    }

    override fun onSpeakerToggle(isEnabled: Boolean) {
        runOnUiThread {
            btnSpeaker?.setImageResource(if (isEnabled) R.drawable.ic_speaker_on else R.drawable.ic_speaker_off)
        }
    }

    override fun onCamSwitch(camSide: CamSide) {
        runOnUiThread {
            localVideoRenderer?.setMirror(camSide == CamSide.FRONT_FACING)
        }
    }

    private fun setClickListeners() {
        btnCancelCall.setOnClickListener {
            if (denied) return@setOnClickListener

            denied = true
            CallEvent.localCancel(this@CallActivity)
            releaseVideoViews()
            finish()
        }
        btnEnd.setOnClickListener {
            if (denied) return@setOnClickListener

            denied = true
            CallEvent.localEnd(this@CallActivity)
            releaseVideoViews()
            finish()
        }
        btnDeclineCall.setOnClickListener {
            if (denied) return@setOnClickListener

            denied = true
            CallEvent.localDecline(this@CallActivity)
            releaseVideoViews()
            finish()
        }
        btnAcceptCall.setOnClickListener {
            if (accepted) return@setOnClickListener

            accepted = true
            CallEvent.stopIncomingRinger(this@CallActivity)
            TedPermission.with(this@CallActivity)
                .setPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS
                ).setPermissionListener(object : PermissionListener {
                    override fun onPermissionGranted() {
                        CallEvent.acceptIncomingCall(this@CallActivity)
                    }

                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                        CallEvent.localDecline(this@CallActivity)
                        releaseVideoViews()
                        finish()
                    }
                }).check()
        }

        btnCam.setOnClickListener { CallEvent.localCamToggle(this@CallActivity) }
        btnMic.setOnClickListener { CallEvent.localMicToggle(this@CallActivity) }
        btnSwitchCam.setOnClickListener { CallEvent.localCamSwitch(this@CallActivity) }
        btnSpeaker.setOnClickListener {
            CallEvent.localSpeakerToggle(
                this@CallActivity,
                callService?.let {
                    !it.isSpeakerEnabled
                } ?: false
            )
        }
    }

    private fun setUpActiveCallView() {
        clIncomingCallRoot.visibility = GONE
        clOutgoingCallRoot.visibility = GONE
        clActiveCallRoot.visibility = VISIBLE
        callService?.let {
            localVideoRenderer.visibility = if (it.localVideoEnabled) VISIBLE else GONE

            viewBackground.visibility = if (it.remoteVideoEnabled) GONE else VISIBLE
            clControlBtns.visibility = VISIBLE
        }
    }

    private fun setUpIncomingCallView(user: User) {
        clActiveCallRoot.visibility = GONE
        clOutgoingCallRoot.visibility = GONE
        clIncomingCallRoot.visibility = VISIBLE
        tvNameIncoming.text = user.name
    }

    private fun setUpOutgoingCallView(user: User) {
        clIncomingCallRoot.visibility = GONE
        clActiveCallRoot.visibility = GONE
        clOutgoingCallRoot.visibility = VISIBLE
        tvNameOutgoing.text = user.name
    }

    @Synchronized
    private fun releaseVideoViews() {
        localVideoRenderer?.release()
        remoteVideoRenderer?.release()
    }

    private fun bindCallService() {
        bindService(
            Intent(this@CallActivity, CallService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun unbindCallService() {
        unbindService(connection)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CallActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}