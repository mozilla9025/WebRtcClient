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
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.rtcmeetings.R
import app.rtcmeetings.base.BaseFragment
import app.rtcmeetings.data.db.dbentity.Contact
import app.rtcmeetings.data.db.dbentity.toUser
import app.rtcmeetings.data.entity.User
import app.rtcmeetings.network.result.Status
import app.rtcmeetings.network.ws.WsService
import app.rtcmeetings.util.i
import app.rtcmeetings.util.logd
import app.rtcmeetings.webrtc.CallEvent
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.dialog_add_contact.view.*
import kotlinx.android.synthetic.main.dialog_find_contact.view.*
import kotlinx.android.synthetic.main.fragment_contacts.*
import javax.inject.Inject

class ContactsFragment : BaseFragment() {

    @Inject
    lateinit var viewModel: ContactsViewModel

    private var wsService: WsService? = null
    private var contactsAdapter: ContactsAdapter? = null

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
        toolbarView.setOnActionClickListener { showSearchDialog() }

        searchView.onTextChangedCallback = {
            logd(it)
            contactsAdapter?.filter?.filter(it)
        }
    }

    private fun showSearchDialog() {
        val view = LayoutInflater.from(context!!).inflate(R.layout.dialog_find_contact, null)

        AlertDialog.Builder(context!!)
            .setCancelable(false)
            .setView(view)
            .setPositiveButton("Find") { dialog, _ ->
                val text = view.etSearch.text
                text?.let {
                    if (text.isNotBlank() && !text.contains("^[a-zA-Z]*\$")) {
                        viewModel.findUser(text.toString().i)
                        dialog.dismiss()
                    } else Toast.makeText(context!!, "ID has wrong type", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showAddDialog(user: User) {
        val view = LayoutInflater.from(context!!).inflate(R.layout.dialog_add_contact, null)
        view.tvUserName.text = user.name

        AlertDialog.Builder(context!!)
            .setCancelable(false)
            .setView(view)
            .setPositiveButton("Add") { dialog, _ ->
                viewModel.addContact(user)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
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
                        contactsAdapter = ContactsAdapter(it.data) { contact ->
                            startCall(contact)
                        }
                        adapter = contactsAdapter
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
                    viewModel.getContacts()
                }
                Status.LOADING -> {
                }
                Status.FAILURE -> {
                }
            }
        })

        viewModel.userLiveData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.let { user ->
                        showAddDialog(user)
                    }
                }
                Status.LOADING -> {
                }
                Status.FAILURE -> {
                    Toast.makeText(context!!, "User not found", Toast.LENGTH_SHORT).show()
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
