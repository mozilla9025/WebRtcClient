package app.rtcmeetings.domain.usecase.impl

import app.rtcmeetings.domain.usecase.LogInUseCase
import app.rtcmeetings.domain.respository.AuthRepository
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LogInUseCaseImpl @Inject constructor(
    private val authRepository: AuthRepository
) : LogInUseCase {

    override fun execute(email: String, password: String): Completable {
        return authRepository.logIn(email, password)
            .subscribeOn(Schedulers.io())
    }
}