package app.rtcmeetings.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import app.rtcmeetings.R
import app.rtcmeetings.base.BaseFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_login.*
import javax.inject.Inject

class LogInFragment : BaseFragment() {

    @Inject
    lateinit var viewModel: LogInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.loginliveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                true -> Navigation.findNavController(view).navigate(LogInFragmentDirections.actionLogInFragmentToMainFragment())
                else -> Toast.makeText(context!!, "Log in failed", Toast.LENGTH_SHORT).show()
            }
        })
        btnLogin.setOnClickListener {
            val email = etEmail.text?.toString()
            val password = etPassword.text?.toString()

            if (email.isNullOrBlank() || password.isNullOrBlank()) {
                Toast.makeText(context!!, "Wrong data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.logIn(email, password)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        view?.let {
            Navigation.findNavController(it).navigateUp()
        }
    }
}