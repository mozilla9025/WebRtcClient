package app.rtcmeetings.ui.module.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.rtcmeetings.R
import app.rtcmeetings.base.BaseFragment
import app.rtcmeetings.network.result.Status
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_contacts.*
import javax.inject.Inject

class ContactsFragment : BaseFragment() {

    @Inject
    lateinit var viewModel: ContactsViewModel

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeData()
        viewModel.getContacts()

        toolbarView.setOnActionLeftClickListener { onBackPressed() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Navigation.findNavController(view!!).navigateUp()
    }

    private fun observeData() {
        viewModel.getContactsLiveData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    rvContacts.run {
                        layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
                        adapter = ContactsAdapter(it.data)
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
}