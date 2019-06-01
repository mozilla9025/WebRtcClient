package app.rtcmeetings.webrtc

import android.content.Context
import android.content.Intent

object ServiceActionHelper {
    fun start(context: Context) {

    }

    private fun getIntent(context: Context): Intent = Intent(context, CallService::class.java)
}