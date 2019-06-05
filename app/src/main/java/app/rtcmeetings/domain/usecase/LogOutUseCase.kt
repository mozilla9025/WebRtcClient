package app.rtcmeetings.domain.usecase

import io.reactivex.Completable

interface LogOutUseCase {
    fun execute(): Completable
}