package app.rtcmeetings.domain.usecase

import io.reactivex.Single

interface CheckAuthUseCase {
    fun execute(): Single<Boolean>
}