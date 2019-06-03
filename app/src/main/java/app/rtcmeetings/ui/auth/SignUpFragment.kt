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
import app.rtcmeetings.util.logi
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_signup.*
import javax.inject.Inject

class SignUpFragment : BaseFragment() {

    @Inject
    lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.signInLiveData.observe(viewLifecycleOwner, Observer {
            logi("$it")
        })

        btnSignUp.setOnClickListener {
            val email = etEmail.text?.toString()
            val name = etName.text?.toString()
            val password = etPassword.text?.toString()

            if (email == null || name == null || password == null) {
                Toast.makeText(context!!, "Wrong data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(context!!, "Password is too short", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.signUp(email, name, password)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        view?.let {
            Navigation.findNavController(it).navigateUp()
        }
    }
}