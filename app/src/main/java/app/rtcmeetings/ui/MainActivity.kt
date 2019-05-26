package app.rtcmeetings.ui

import android.os.Bundle
import app.rtcmeetings.R
import app.rtcmeetings.base.BaseActivity

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}