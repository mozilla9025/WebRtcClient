package app.rtcmeetings.network.api

import app.rtcmeetings.data.entity.User
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {

    @GET("/account/{id}")
    fun getUser(@Path("id") id: Int): Single<User>

    @GET("/account")
    fun getMe(): Single<User>

}