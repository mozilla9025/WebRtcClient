package app.rtcmeetings.domain.respository

import io.reactivex.Completable

interface AuthRepository {
    fun logIn(email: String, password: String): Completable
    fun logOut():Completable
}