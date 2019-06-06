package app.rtcmeetings.domain.usecase.impl

import app.rtcmeetings.domain.respository.CallRepository
import app.rtcmeetings.domain.usecase.CallUseCase
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CallUseCaseImpl @Inject constructor(
        private val callRepository: CallRepository
) : CallUseCase {

    override fun startCall(socketId: String, sdp: String, userId: Int): Single<String> {
        return callRepository.startCall(socketId, sdp, userId)
                .subscribeOn(Schedulers.io())
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