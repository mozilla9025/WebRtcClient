package app.rtcmeetings.domain.usecase

import io.reactivex.Completable
import io.reactivex.Single

interface CallUseCase {
    fun startCall(socketId: String,
                  sdp: String,
                  userId: Int): Single<String>

    fun getIncomingCall(): Single<String>

    fun cancelCall(callId: String): Completable

    fun acceptCall(callId: Int,
                   sdp: String,
                   socketId: String): Completable

    fun declineCall(callId: String): Completable

    fun finishCall(callId: String): Completable
}