package app.rtcmeetings.ui.module.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import app.rtcmeetings.R
import app.rtcmeetings.base.BaseFragment
import app.rtcmeetings.helper.CheckHelper
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.ibBack
import kotlinx.android.synthetic.main.fragment_login.rfivEmail
import kotlinx.android.synthetic.main.fragment_login.rfivPassword
import kotlinx.android.synthetic.main.fragment_signup.*
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
        ibBack.setOnClickListener { onBackPressed() }

        viewModel.loginliveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                true -> Navigation.findNavController(view).navigate(LogInFragmentDirections.actionLogInFragmentToMainFragment())
                else -> Toast.makeText(
                    context!!,
                    getString(R.string.login_framgent_login_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        btnSignIn.setOnClickListener {
            checkFieldsAndSignIn()
        }
    }

    private fun checkFieldsAndSignIn() {
        val checkHelper = CheckHelper(context)

        var isError = false

        val emailValid = checkHelper.checkEmailValid(rfivEmail.text)
        if (!emailValid.isValid) {
            rfivEmail.setError(emailValid.errorMessage)
            isError = true
        }

        val passwordValid = checkHelper.checkFieldIsEmpty(rfivPassword.text, true)
        if (!passwordValid.isValid) {
            rfivPassword.setError(passwordValid.errorMessage)
            isError = true
        }

        if (isError) {
            return
        }

        viewModel.logIn(emailValid.formattedValue, passwordValid.formattedValue)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        view?.let {
            Navigation.findNavController(it).navigateUp()
        }
    }
}