package app.rtcmeetings.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.rtcmeetings.R
import app.rtcmeetings.base.BaseFragment

class MainFragment : BaseFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        activity?.finish()
    }

}