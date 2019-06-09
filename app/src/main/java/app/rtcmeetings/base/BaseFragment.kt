package app.rtcmeetings.base

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import app.rtcmeetings.util.logd
import app.rtcmeetings.util.logi

abstract class BaseFragment : Fragment() {

    private val backPressedCallback = object : OnBackPressedCallback(backPressedEnabled()) {
        override fun handleOnBackPressed() {
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        logi("onCreateView $activity ${activity?.onBackPressedDispatcher?.hasEnabledCallbacks()}")
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    override fun onStop() {
        super.onStop()
        backPressedCallback.remove()
    }

    open fun backPressedEnabled(): Boolean = true

    open fun onBackPressed() {
        logd("${this.javaClass.simpleName} OnBackPressed")
    }
}