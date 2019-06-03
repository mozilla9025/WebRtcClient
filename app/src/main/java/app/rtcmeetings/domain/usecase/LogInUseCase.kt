package app.rtcmeetings.domain.usecase

import io.reactivex.Completable

interface LogInUseCase {
    fun execute(
        email: String,
        password: String
    ): Completable
}