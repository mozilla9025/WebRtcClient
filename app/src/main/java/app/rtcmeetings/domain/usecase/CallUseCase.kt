package app.rtcmeetings.domain.usecase

interface CallUseCase {
    fun start()

    fun end()

    fun decline()

    fun cancel()

    fun accept()
}