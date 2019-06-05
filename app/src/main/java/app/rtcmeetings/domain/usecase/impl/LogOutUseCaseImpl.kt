package app.rtcmeetings.domain.usecase.impl

import app.rtcmeetings.domain.respository.AuthRepository
import app.rtcmeetings.domain.usecase.LogOutUseCase
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LogOutUseCaseImpl @Inject constructor(
        private val authRepository: AuthRepository
) : LogOutUseCase {

    override fun execute(): Completable {
        return authRepository.logOut()
                .subscribeOn(Schedulers.io())
    }
}