package app.rtcmeetings.webrtc.audio

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import java.io.IOException

class IncomingRinger(private val context: Context) {

    private var vibrator: Vibrator
    private var audioManager: AudioManager
    private var player: MediaPlayer? = null

    init {
        vibrator = context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
    }

    fun start(uri: Uri?, vibrate: Boolean) {
        if (player != null) player!!.release()
        if (uri != null) player = createPlayer(uri)

        val ringerMode = audioManager.ringerMode

        if (shouldVibrate(player, ringerMode, vibrate)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(VIBRATE_PATTERN, 1))
            } else {
                vibrator.vibrate(VIBRATE_PATTERN, 1)
            }
        }

        if (player != null && ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            try {
                if (!player!!.isPlaying) {
                    player!!.prepare()
                    player!!.start()
                }
            } catch (e: IllegalStateException) {
                player = null
            } catch (e: IOException) {
                player = null
            }
        }
    }

    fun stop() {
        if (player != null) {
            player!!.release()
            player = null
        }

        vibrator.cancel()
    }

    private fun shouldVibrate(player: MediaPlayer?, ringerMode: Int, vibrate: Boolean): Boolean {
        return if (player == null) {
            true
        } else {
            if (!vibrator.hasVibrator())
                return false

            return if (vibrate)
                ringerMode != AudioManager.RINGER_MODE_SILENT
            else
                ringerMode == AudioManager.RINGER_MODE_VIBRATE
        }
    }

    private fun createPlayer(ringtoneUri: Uri): MediaPlayer? {
        try {
            val mediaPlayer = MediaPlayer()

            mediaPlayer.setOnErrorListener(MediaPlayerErrorListener())
            mediaPlayer.setDataSource(context, ringtoneUri)
            mediaPlayer.isLooping = true
            mediaPlayer.setAudioAttributes(AudioAttributes.Builder().apply {
                setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            }.build())

            return mediaPlayer
        } catch (e: IOException) {
            return null
        }
    }

    private inner class MediaPlayerErrorListener : MediaPlayer.OnErrorListener {
        override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            player = null
            return false
        }
    }

    companion object {
        private val VIBRATE_PATTERN = longArrayOf(0, 1000, 1000)
    }
}