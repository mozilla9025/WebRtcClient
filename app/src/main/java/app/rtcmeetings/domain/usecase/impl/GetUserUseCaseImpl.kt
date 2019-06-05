package app.rtcmeetings.domain.usecase.impl

import app.rtcmeetings.data.entity.User
import app.rtcmeetings.domain.respository.UserRepository
import app.rtcmeetings.domain.usecase.GetUserUseCase
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetUserUseCaseImpl @Inject constructor(
        private val userRepository: UserRepository
) : GetUserUseCase {

    override fun getUserById(id: Int): Single<User> {
        return userRepository.getUserById(id)
                .subscribeOn(Schedulers.io())
    }

    override fun getMe(): Single<User> {
        return userRepository.getMe()
                .subscribeOn(Schedulers.io())
    }
}