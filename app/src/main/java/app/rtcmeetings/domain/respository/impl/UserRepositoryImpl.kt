package app.rtcmeetings.domain.respository.impl

import app.rtcmeetings.data.entity.User
import app.rtcmeetings.domain.respository.UserRepository
import app.rtcmeetings.network.api.UserApi
import io.reactivex.Single
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
        private val userApi: UserApi
) : UserRepository {

    override fun getUserById(id: Int): Single<User> {
        return userApi.getUser(id)
    }

    override fun getMe(): Single<User> {
        return userApi.getMe()
    }
}