package app.rtcmeetings.ui.module.contacts

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.rtcmeetings.R
import app.rtcmeetings.base.BaseFragment
import app.rtcmeetings.data.db.dbentity.Contact
import app.rtcmeetings.data.db.dbentity.toUser
import app.rtcmeetings.network.result.Status
import app.rtcmeetings.network.ws.WsService
import app.rtcmeetings.webrtc.CallEvent
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_contacts.*
import javax.inject.Inject

class ContactsFragment : BaseFragment() {

    @Inject
    lateinit var viewModel: ContactsViewModel

    private var wsService: WsService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onStart() {
        super.onStart()
        activity?.bindService(
                Intent(context!!, WsService::class.java),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        super.onStop()
        activity?.unbindService(serviceConnection)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeData()
        viewModel.getContacts()

        toolbarView.setOnActionLeftClickListener { onBackPressed() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Navigation.findNavController(view!!).navigateUp()
    }

    private fun startCall(contact: Contact) {
        wsService?.getSocketId()?.let { socketId ->
            CallEvent.startCall(context!!, contact.toUser(), socketId)
        } ?: Toast.makeText(context!!, "No WS connection", Toast.LENGTH_SHORT).show()
    }

    private fun observeData() {
        viewModel.getContactsLiveData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    rvContacts.run {
                        layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
                        adapter = ContactsAdapter(it.data) { contact ->
                            startCall(contact)
                        }
                    }
                }
                Status.LOADING -> {
                }
                Status.FAILURE -> {
                }
            }
        })

        viewModel.addContactsLiveData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                }
                Status.LOADING -> {
                }
                Status.FAILURE -> {
                }
            }
        })
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            wsService = (service as WsService.LocalBinder).service
        }
    }
}
