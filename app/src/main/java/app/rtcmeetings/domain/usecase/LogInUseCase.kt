package app.rtcmeetings.domain.usecase

interface LogInUseCase {
    fun execute(email: String,
                password: String)
}