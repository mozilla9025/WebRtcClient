package app.rtcmeetings.webrtc.audio

import android.media.MediaPlayer
import android.content.Context
import android.media.AudioAttributes

class OutgoingRinger(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    enum class Type {
        RINGING,
        BUSY
    }

    fun start(type: Type) {
        val soundId: Int

        if (mediaPlayer != null) {
            mediaPlayer!!.release()
        }

        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setAudioAttributes(AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build())
        mediaPlayer!!.isLooping = true

//        val packageName = context.getPackageName()
//        val dataUri = Uri.parse("android.resource://$packageName/$soundId")

//            mediaPlayer!!.setDataSource(context, dataUri)
//            mediaPlayer!!.prepare()
//            mediaPlayer!!.start()


    }

    fun stop() {
        if (mediaPlayer == null) return
        mediaPlayer!!.release()
        mediaPlayer = null
    }

    companion object {

        private val TAG = OutgoingRinger::class.java.simpleName
    }
}