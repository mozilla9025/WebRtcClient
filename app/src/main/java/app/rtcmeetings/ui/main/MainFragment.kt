package app.rtcmeetings.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import app.rtcmeetings.R
import app.rtcmeetings.base.BaseFragment
import app.rtcmeetings.util.i
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_main.*
import javax.inject.Inject

class MainFragment : BaseFragment() {

    @Inject
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
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

        btnStartCall.setOnClickListener {
            etId.text?.let {
                if (it.isNotBlank() && !it.contains("^[a-zA-Z]*\$"))
                    viewModel.getUser(it.toString().i)
                else Toast.makeText(context!!, "ID has wrong type", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogOut.setOnClickListener { viewModel.logOut() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        activity?.finish()
    }

}