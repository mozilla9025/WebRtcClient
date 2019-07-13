package app.rtcmeetings.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import app.rtcmeetings.R
import app.rtcmeetings.base.BaseActivity
import app.rtcmeetings.data.entity.User
import app.rtcmeetings.util.logw
import app.rtcmeetings.webrtc.*
import app.rtcmeetings.webrtc.video.CamSide
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.include_active_call.*
import kotlinx.android.synthetic.main.include_incoming_call.*
import kotlinx.android.synthetic.main.include_outgoing_call.*
import org.webrtc.RendererCommon


class CallActivity : BaseActivity(), CallEventListener, DeviceEventListener {

    private var callService: CallService? = null

    private var accepted = false
    private var denied = false

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            callService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            callService = (service as CallService.LocalBinder).service
            callService!!.run {
                remoteVideo.run {
                    init(eglBase.eglBaseContext, null)
                    setEnableHardwareScaler(true)
                }
                localVideo.run {
                    init(eglBase.eglBaseContext, null)
                    setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED)
                    setMirror(true)
                    setZOrderMediaOverlay(true)
                    setEnableHardwareScaler(true)
                }
                setCallEventListener(this@CallActivity)
                setDeviceEventListener(this@CallActivity)

                setTargets(localVideo, remoteVideo)
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
    }

    override fun onStart() {
        super.onStart()
        bindCallService()
        setClickListeners()
    }

    override fun onResume() {
        super.onResume()
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
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    override fun onStop() {
        super.onStop()
        releaseVideoViews()
        unbindCallService()
    }

    override fun onConnect() {
        runOnUiThread {
            setUpActiveCallView()
        }
    }

    override fun onCreatingConnection() {
        runOnUiThread {
            Toast.makeText(this@CallActivity, "Connection creating", Toast.LENGTH_SHORT).show()
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
            releaseVideoViews()
            finish()
        }
    }

    override fun onRemoteUserStopStream() {
        runOnUiThread {
            remoteVideo.visibility = View.GONE
        }
    }

    override fun onRemoteUserStartStream() {
        runOnUiThread {
            remoteVideo.visibility = View.VISIBLE
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
            localVideo.visibility = if (isEnabled) View.VISIBLE else View.GONE
        }
    }

    override fun onMicToggle(isEnabled: Boolean) {
    }

    override fun onCamSwitch(camSide: CamSide) {
        runOnUiThread {
            localVideo.setMirror(camSide == CamSide.FRONT_FACING)
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
    }

    private fun setUpActiveCallView() {
        clIncomingCallRoot.visibility = View.GONE
        clOutgoingCallRoot.visibility = View.GONE
        clActiveCallRoot.visibility = View.VISIBLE
    }

    private fun setUpIncomingCallView(user: User) {
        clActiveCallRoot.visibility = View.GONE
        clOutgoingCallRoot.visibility = View.GONE
        clIncomingCallRoot.visibility = View.VISIBLE
        tvNameIncoming.text = user.name
    }

    private fun setUpOutgoingCallView(user: User) {
        clIncomingCallRoot.visibility = View.GONE
        clActiveCallRoot.visibility = View.GONE
        clOutgoingCallRoot.visibility = View.VISIBLE
        tvNameOutgoing.text = user.name
    }

    private fun releaseVideoViews() {
        localVideo?.release()
        remoteVideo?.release()
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