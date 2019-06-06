package app.rtcmeetings.domain.respository.impl

import app.rtcmeetings.domain.respository.CallRepository
import app.rtcmeetings.network.api.CallApi
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class CallRepositoryImpl @Inject constructor(
        private val callApi: CallApi
) : CallRepository {

    override fun startCall(socketId: String, sdp: String, userId: Int): Single<String> {
        return callApi.startCall(socketId, sdp, userId)
                .map { return@map it.body()?.string() }
    }

    override fun getIncomingCall(): Single<String> {
        TODO()
    }

    override fun cancelCall(callId: String): Completable {
        TODO()

    }

    override fun acceptCall(callId: Int, sdp: String, socketId: String): Completable {
        TODO()

    }

    override fun declineCall(callId: String): Completable {
        TODO()

    }

    override fun finishCall(callId: String): Completable {
        TODO()

    }
}