package app.rtcmeetings.domain.usecase.respository

import io.reactivex.Completable

interface AuthRepository {
    fun logIn(email: String, password: String): Completable
}