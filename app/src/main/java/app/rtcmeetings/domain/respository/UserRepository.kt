package app.rtcmeetings.domain.respository

import app.rtcmeetings.data.entity.User
import io.reactivex.Single

interface UserRepository {
    fun getUserById(id: Int): Single<User>
    fun getMe(): Single<User>
    fun getUserByEmail(email: String): Single<User>
}