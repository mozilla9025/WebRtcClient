package app.rtcmeetings.domain.respository

import io.reactivex.Completable
import io.reactivex.Single

interface CallRepository {
    fun startCall(socketId: String, sdp: String, userId: Int): Single<String>

    fun getIncomingCall(): Single<String>

    fun cancelCall(callId: String): Completable

    fun acceptCall(callId: Int, sdp: String, socketId: String): Completable

    fun declineCall(callId: String): Completable

    fun finishCall(callId: String): Completable
}