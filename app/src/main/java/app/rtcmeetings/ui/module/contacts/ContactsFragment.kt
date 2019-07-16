package app.rtcmeetings.ui.module.contacts

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
import app.rtcmeetings.helper.CheckHelper
import app.rtcmeetings.network.result.Status
import app.rtcmeetings.network.ws.WsService
import app.rtcmeetings.util.logd
import app.rtcmeetings.webrtc.CallEvent
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.dialog_add_contact.view.*
import kotlinx.android.synthetic.main.dialog_find_contact.view.*
import kotlinx.android.synthetic.main.fragment_contacts.*
import javax.inject.Inject

class ContactsFragment : BaseFragment() {

    @Inject
    lateinit var viewModel: ContactsViewModel

    private var wsService: WsService? = null

    private val adapter: ContactsAdapter by lazy {
        ContactsAdapter { contact ->
            startCall(contact)
        }
    }

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

        rvContacts.run {
            layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
            adapter = this@ContactsFragment.adapter
        }
        toolbarView.run {
            setOnActionLeftClickListener { onBackPressed() }
            setOnActionClickListener { showSearchDialog() }
        }

        searchView.onTextChangedCallback = {
            logd(it)
            adapter.filter.filter(it)
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
                        if (text.isNotBlank() && CheckHelper.isValidEmail(text)) {
                            viewModel.findUser(text.toString())
                            dialog.dismiss()
                        } else Toast.makeText(context!!, "Invalid email", Toast.LENGTH_SHORT).show()
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
        TedPermission.with(activity!!)
                .setPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.RECORD_AUDIO
                )
                .setPermissionListener(object : PermissionListener {
                    override fun onPermissionGranted() {
                        wsService?.getSocketId()?.let { socketId ->
                            CallEvent.startCall(context!!, contact.toUser(), socketId)
                        }
                                ?: Toast.makeText(context!!, "No WS connection", Toast.LENGTH_SHORT).show()
                    }

                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                        Toast.makeText(context!!, "Required permissions denied", Toast.LENGTH_SHORT).show()
                    }
                }).check()
    }

    private fun observeData() {
        viewModel.getContactsLiveData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.let { list -> adapter.updateData(list) }
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
