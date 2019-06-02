package app.rtcmeetings.network

import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UserApi {
    @FormUrlEncoded
    @POST("user/login")
    fun login(@Field("login") login: String,
              @Field("password") password: String): Single<String> //access token
}