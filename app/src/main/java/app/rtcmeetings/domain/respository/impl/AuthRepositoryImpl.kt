package app.rtcmeetings.domain.respository.impl

import app.rtcmeetings.data.AuthStorage
import app.rtcmeetings.domain.respository.AuthRepository
import app.rtcmeetings.network.api.AuthApi
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
        private val authApi: AuthApi,
        private val authStorage: AuthStorage,
        private val gson: Gson
) : AuthRepository {
    override fun logIn(email: String, password: String): Completable {
        return authApi.login(email, password)
                .map {
                    if (it.isSuccessful) {
                        it.body()?.let { body ->
                            val json = gson.fromJson(body.string(), JsonObject::class.java)
                            return@map json.get("accessToken").asString!!
                        } ?: throw IllegalStateException("Access Token is not found")
                    } else {
                        throw IllegalStateException("Access Token is not found")
                    }
                }.flatMapCompletable { authStorage.setToken(it) }
    }

    override fun logOut(): Completable {
        return authStorage.clearData()
    }
}