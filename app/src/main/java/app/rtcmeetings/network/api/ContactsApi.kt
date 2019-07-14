package app.rtcmeetings.network.api

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ContactsApi {

    @GET("/contacts")
    fun getContacts(): Single<Response<ResponseBody>>

    @POST("/contacts/{account_id}")
    fun addContact(@Path("account_id") accountId: Int): Single<Response<ResponseBody>>

    @DELETE("/contacts/{account_id}")
    fun removeComtact(@Path("account_id") accountId: Int): Single<Response<ResponseBody>>
}
