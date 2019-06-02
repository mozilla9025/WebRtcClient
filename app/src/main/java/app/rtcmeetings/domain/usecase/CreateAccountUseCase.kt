package app.rtcmeetings.domain.usecase

interface CreateAccountUseCase {
    fun execute(email: String,
                password: String)
}