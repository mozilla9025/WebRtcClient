package app.rtcmeetings.ui.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import app.rtcmeetings.R
import app.rtcmeetings.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_start.*

class StartFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSignUp.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(StartFragmentDirections.actionStartFragmentToSignUpFragment())
        }

        btnLogIn.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(StartFragmentDirections.actionStartFragmentToLogInFragment())
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        activity?.finish()
    }
}