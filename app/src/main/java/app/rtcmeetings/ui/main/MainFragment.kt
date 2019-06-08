package app.rtcmeetings.ui.main

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import app.rtcmeetings.R
import app.rtcmeetings.base.BaseFragment
import app.rtcmeetings.network.ws.WsService
import app.rtcmeetings.util.i
import app.rtcmeetings.webrtc.CallEvent
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_main.*
import javax.inject.Inject

class MainFragment : BaseFragment() {

    @Inject
    lateinit var viewModel: MainViewModel

    private var wsService: WsService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel.startWsService(context!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.logOutLiveData.observe(viewLifecycleOwner, Observer {
            when {
                it -> Navigation.findNavController(view)
                    .navigate(MainFragmentDirections.actionMainFragmentToSplashScreenFragment())
                else -> Toast.makeText(context!!, "Unable to log out", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.userLiveData.observe(viewLifecycleOwner, Observer {
            it?.let { user ->
                wsService?.getSocketId()?.let { socketId ->
                    CallEvent.startCall(context!!, user, socketId)
                } ?: Toast.makeText(context!!, "No WS connection", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(context!!, "User not found", Toast.LENGTH_SHORT).show()
        })

        btnStartCall.setOnClickListener {
            etId.text?.let {
                if (it.isNotBlank() && !it.contains("^[a-zA-Z]*\$")) {
                    TedPermission.with(activity!!)
                        .setPermissions(
                            Manifest.permission.CAMERA,
                            Manifest.permission.MODIFY_AUDIO_SETTINGS,
                            Manifest.permission.RECORD_AUDIO
                        )
                        .setPermissionListener(object : PermissionListener {
                            override fun onPermissionGranted() {
                                viewModel.getUser(it.toString().i)
                            }

                            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                                Toast.makeText(context!!, "Required permissions denied", Toast.LENGTH_SHORT).show()
                            }
                        }).check()
                } else Toast.makeText(context!!, "ID has wrong type", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogOut.setOnClickListener {
            viewModel.disconnectWs(context!!)
            viewModel.logOut()
        }
    }

    override fun onResume() {
        super.onResume()
        bindService()
    }

    private fun bindService() {
        activity?.bindService(
            Intent(context!!, WsService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            wsService = (service as WsService.LocalBinder).service
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        activity?.finish()
    }
}