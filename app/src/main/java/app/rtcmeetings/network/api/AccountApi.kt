package app.rtcmeetings.network.api

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface AccountApi {

    @GET("/account/{id}")
    fun getAccount(@Path("id") id: Int): Single<Response<ResponseBody>>

    @GET("/account")
    fun getMyAccount(): Single<Response<ResponseBody>>

}