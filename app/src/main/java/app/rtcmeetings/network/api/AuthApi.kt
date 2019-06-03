package app.rtcmeetings.network.api

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApi {
    @FormUrlEncoded
    @POST("/authorization/login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Single<Response<ResponseBody>> //access token

    @FormUrlEncoded
    @POST("/account")
    fun createAccount(
        @Field("email") email: String,
        @Field("name") name: String,
        @Field("password") password: String
    ): Single<Response<ResponseBody>> //access token
}