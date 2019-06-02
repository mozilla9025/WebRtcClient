package app.rtcmeetings.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import app.rtcmeetings.R
import app.rtcmeetings.base.BaseFragment
import dagger.android.support.AndroidSupportInjection
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

    override fun onBackPressed() {
        super.onBackPressed()
        view?.let {
            Navigation.findNavController(it).navigateUp()
        }
    }
}