package app.rtcmeetings.webrtc.audio

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.media.RingtoneManager

class CallAudioManager(private val context: Context) {

    private val incomingRinger: IncomingRinger
    private val outgoingRinger: OutgoingRinger
    private val audioManager: AudioManager

    init {
        audioManager = context.applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
        incomingRinger = IncomingRinger(context.applicationContext)
        outgoingRinger = OutgoingRinger(context.applicationContext)
    }

    fun initializeAudioForCall() {
        audioManager.requestAudioFocus(
            null, AudioManager.STREAM_VOICE_CALL,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
        )
    }

    fun startIncomingRinger(vibrate: Boolean) {
        val speaker = !audioManager.isWiredHeadsetOn && !audioManager.isBluetoothScoOn
        audioManager.mode = AudioManager.MODE_RINGTONE
        audioManager.isMicrophoneMute = false
        audioManager.isSpeakerphoneOn = speaker

        incomingRinger.start(
            RingtoneManager.getActualDefaultRingtoneUri(
                context.applicationContext,
                RingtoneManager.TYPE_RINGTONE
            ), vibrate
        )
    }

    fun startOutgoingRinger(type: OutgoingRinger.Type) {
        audioManager.isMicrophoneMute = false

        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        outgoingRinger.start(type)
    }

    fun stopIncomingRinger() {
        incomingRinger.stop()
    }

    fun startCommunication(preserveSpeakerphone: Boolean) {
        incomingRinger.stop()
        outgoingRinger.stop()

        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        if (!preserveSpeakerphone) {
            audioManager.isSpeakerphoneOn = false
        }
    }

    fun setSpeakerEnabled(enabled: Boolean) {
        audioManager.isSpeakerphoneOn = enabled
    }

    fun stop(playDisconnected: Boolean) {
        incomingRinger.stop()
        outgoingRinger.stop()

        if (audioManager.isBluetoothScoOn) {
            audioManager.isBluetoothScoOn = false
            audioManager.stopBluetoothSco()
        }

        audioManager.isSpeakerphoneOn = false
        audioManager.isMicrophoneMute = false
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.abandonAudioFocus(null)
    }
}