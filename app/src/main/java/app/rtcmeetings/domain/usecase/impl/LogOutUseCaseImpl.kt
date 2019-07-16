package app.rtcmeetings.domain.usecase.impl

import app.rtcmeetings.data.db.DBCleaner
import app.rtcmeetings.domain.respository.AuthRepository
import app.rtcmeetings.domain.usecase.LogOutUseCase
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LogOutUseCaseImpl @Inject constructor(
        private val authRepository: AuthRepository,
        private val dbCleaner: DBCleaner
) : LogOutUseCase {

    override fun execute(): Completable {
        return authRepository.logOut()
                .andThen(dbCleaner.cleanDatabase())
                .subscribeOn(Schedulers.io())
    }
}