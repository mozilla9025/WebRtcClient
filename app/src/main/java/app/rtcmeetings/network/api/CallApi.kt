package app.rtcmeetings.network.api

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CallApi {

    @FormUrlEncoded
    @POST("call/start")
    fun startCall(@Field("target_user_id") targetUserId: Int,
                  @Field("sdp") sdp: String): Single<String> //call id

    @FormUrlEncoded
    @POST("call/cancel")
    fun cancelCall(@Field("call_id") callId: String): Completable

    @FormUrlEncoded
    @POST("call/accept")
    fun acceptCall(@Field("call_id") callId: String): Completable

    @FormUrlEncoded
    @POST("call/decline")
    fun declineCall(@Field("call_id") callId: String): Completable

    @FormUrlEncoded
    @POST("call/finish")
    fun finishCall(@Field("call_id") callId: String): Completable

}