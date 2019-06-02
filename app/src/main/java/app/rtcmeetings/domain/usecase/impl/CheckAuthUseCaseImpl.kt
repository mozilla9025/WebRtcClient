package app.rtcmeetings.domain.usecase.impl

import app.rtcmeetings.data.AuthStorage
import app.rtcmeetings.domain.usecase.CheckAuthUseCase
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CheckAuthUseCaseImpl @Inject constructor(
        private val authStorage: AuthStorage
) : CheckAuthUseCase {
    override fun execute(): Single<Boolean> {
        return authStorage.hasToken()
                .observeOn(Schedulers.io())
    }
}