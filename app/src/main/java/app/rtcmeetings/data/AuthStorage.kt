package app.rtcmeetings.data

import io.reactivex.Completable
import io.reactivex.Single

interface AuthStorage {

    fun hasToken(): Single<Boolean>
    fun getToken(): Single<String>
    fun setToken(token: String): Completable
}