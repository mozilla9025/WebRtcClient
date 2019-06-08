package app.rtcmeetings.network.api

import app.rtcmeetings.network.request.CallRequest
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface CallApi {

    @POST("call/new")
    fun startCall(@Body request: CallRequest): Single<Response<ResponseBody>>

    @GET("call")
    fun getIncomingCall(): Single<Response<ResponseBody>>

    @POST("call/cancel/{call_id}")
    fun cancelCall(@Path("call_id") callId: Int): Completable

    @POST("call/accept")
    fun acceptCall(@Body request: CallRequest): Completable

    @POST("call/decline/{call_id}")
    fun declineCall(@Path("call_id") callId: Int): Completable

    @POST("call/finish/{call_id}")
    fun finishCall(@Path("call_id") callId: Int): Completable
}