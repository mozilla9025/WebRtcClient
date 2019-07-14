package app.rtcmeetings.ui.module.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import app.rtcmeetings.R
import app.rtcmeetings.base.BaseFragment
import app.rtcmeetings.util.logi
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SplashScreenFragment : BaseFragment() {

    @Inject
    lateinit var viewModel: SplashScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        logi("OnCreateView")
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.authLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                true -> Navigation.findNavController(view)
                        .navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentToMainFragment())
                false -> Navigation.findNavController(view)
                        .navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentToStartFragment())
            }
        })
        viewModel.checkIfAuthorized()
    }
}

