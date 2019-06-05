package app.rtcmeetings.domain.respository

import app.rtcmeetings.data.entity.User
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response

interface UserRepository {
    fun getUserById(id: Int): Single<User>
    fun getMe(): Single<User>
}