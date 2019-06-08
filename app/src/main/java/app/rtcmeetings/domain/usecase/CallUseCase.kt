package app.rtcmeetings.domain.usecase

import io.reactivex.Completable
import io.reactivex.Single

interface CallUseCase {
    fun startCall(
            socketId: String,
            sdp: String,
            userId: Int
    ): Single<Int>

    fun getIncomingCall(): Single<String>

    fun cancelCall(callId: Int): Completable

    fun acceptCall(
            callId: Int,
            sdp: String,
            socketId: String
    ): Completable

    fun declineCall(callId: Int): Completable

    fun finishCall(callId: Int): Completable
}