package app.rtcmeetings.domain

interface CallUseCase {
    fun start()

    fun end()

    fun decline()

    fun cancel()

    fun accept()
}