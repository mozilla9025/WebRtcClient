package app.rtcmeetings.base

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import app.rtcmeetings.util.logd

abstract class BaseFragment : Fragment() {

    private val backPressedCallback = object : OnBackPressedCallback(backPressedEnabled()) {
        override fun handleOnBackPressed() {
            onBackPressed()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(backPressedCallback)
    }

    open fun backPressedEnabled(): Boolean = true

    open fun onBackPressed() {
        logd("${this.javaClass.simpleName} OnBackPressed")
    }
}