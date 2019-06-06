package app.rtcmeetings.network.api

import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface CallApi {
    @FormUrlEncoded
    @POST("call/new")
    fun startCall(@Field("socketID") socketId: String,
                  @Field("sdp") sdp: String,
                  @Field("accountID") userId: Int): Single<Response<ResponseBody>>

    @FormUrlEncoded
    @GET("call")
    fun getIncomingCall(): Single<Response<ResponseBody>>

    @FormUrlEncoded
    @POST("call/cancel")
    fun cancelCall(@Field("call_id") callId: String): Completable

    @FormUrlEncoded
    @POST("call/accept")
    fun acceptCall(@Field("callID") callId: Int,
                   @Field("sdp") sdp: String,
                   @Field("socketID") socketId: String): Completable

    @FormUrlEncoded
    @POST("call/decline")
    fun declineCall(@Field("call_id") callId: String): Completable

    @FormUrlEncoded
    @POST("call/finish")
    fun finishCall(@Field("call_id") callId: String): Completable
}