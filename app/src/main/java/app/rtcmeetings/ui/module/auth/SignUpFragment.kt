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
        ibBack.setOnClickListener { onBackPressed() }
        viewModel.signUpLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                true -> Navigation.findNavController(view)
                    .navigate(SignUpFragmentDirections.actionSignUpFragmentToMainFragment())
                else -> Toast.makeText(context!!, getString(R.string.register_fragment_registration_error), Toast.LENGTH_SHORT).show()
            }
        })

        btnSignUp.setOnClickListener {
            checkFieldsAndSignUp()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        view?.let {
            Navigation.findNavController(it).navigateUp()
        }
    }

    private fun checkFieldsAndSignUp() {
        val checkHelper = CheckHelper(context)

        var isError = false

        val passwordValid = checkHelper.isPasswordValid(rfivPassword.text)
        if (!passwordValid.isValid) {
            rfivPassword.setError(passwordValid.errorMessage)
            isError = true
        }
        val nameValid = checkHelper.isNameValid(rfivName.text)
        if (!nameValid.isValid) {
            rfivName.setError(nameValid.errorMessage)
            isError = true
        }
        val emailValid = checkHelper.checkEmailValid(rfivEmail.text)
        if (!emailValid.isValid) {
            rfivEmail.setError(emailValid.errorMessage)
            isError = true
        }

        if (isError) return

        viewModel.signUp(emailValid.formattedValue, nameValid.formattedValue, passwordValid.formattedValue)
    }
}