package app.rtcmeetings.ui.module

import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.rtcmeetings.R
import app.rtcmeetings.base.BaseActivity

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    companion object {
        fun start(from: Context) {
            val intent = Intent(from, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

            if (from is MainActivity)
                from.finish()

            from.startActivity(intent)
        }
    }
}