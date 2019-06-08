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

    override fun startCall(socketId: String, sdp: String, userId: Int): Single<Int> {
        return callRepository.startCall(socketId, sdp, userId)
                .subscribeOn(Schedulers.io())
    }

    override fun getIncomingCall(): Single<String> {
        TODO()
    }

    override fun cancelCall(callId: Int): Completable {
        return callRepository.cancelCall(callId)
                .subscribeOn(Schedulers.io())
    }

    override fun acceptCall(callId: Int, sdp: String, socketId: String): Completable {
        return callRepository.acceptCall(callId, sdp, socketId)
                .subscribeOn(Schedulers.io())
    }

    override fun declineCall(callId: Int): Completable {
        return callRepository.declineCall(callId)
                .subscribeOn(Schedulers.io())
    }

    override fun finishCall(callId: Int): Completable {
        return callRepository.finishCall(callId)
                .subscribeOn(Schedulers.io())
    }
}