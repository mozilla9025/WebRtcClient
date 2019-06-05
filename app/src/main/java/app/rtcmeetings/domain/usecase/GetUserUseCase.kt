package app.rtcmeetings.domain.usecase

import app.rtcmeetings.data.entity.User
import io.reactivex.Single

interface GetUserUseCase {
    fun getUserById(id: Int): Single<User>
    fun getMe(): Single<User>
}