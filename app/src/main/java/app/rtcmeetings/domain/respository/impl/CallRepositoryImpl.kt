package app.rtcmeetings.domain.respository.impl

import app.rtcmeetings.domain.respository.CallRepository
import app.rtcmeetings.network.api.CallApi
import app.rtcmeetings.network.request.CallRequest
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class CallRepositoryImpl @Inject constructor(
        private val callApi: CallApi,
        private val gson: Gson
) : CallRepository {

    override fun startCall(socketId: String, sdp: String, userId: Int): Single<Int> {
        return callApi.startCall(
                CallRequest().apply {
                    this.socketId = socketId
                    this.sdp = sdp
                    this.userId = userId
                }
        ).map {
            it.body()?.let { body ->
                val json = gson.fromJson(body.string(), JsonObject::class.java)
                return@map json.get("id").asInt
            } ?: throw IllegalStateException("Body is null")
        }
    }

    override fun getIncomingCall(): Single<String> {
        TODO()
    }

    override fun cancelCall(callId: Int): Completable {
        return callApi.cancelCall(callId)
    }

    override fun acceptCall(callId: Int, sdp: String, socketId: String): Completable {
        return callApi.acceptCall(
                CallRequest().apply {
                    this.callId = callId
                    this.sdp = sdp
                    this.socketId = socketId
                }
        )
    }

    override fun declineCall(callId: Int): Completable {
        return callApi.declineCall(callId)
    }

    override fun finishCall(callId: Int): Completable {
        return callApi.finishCall(callId)
    }
}