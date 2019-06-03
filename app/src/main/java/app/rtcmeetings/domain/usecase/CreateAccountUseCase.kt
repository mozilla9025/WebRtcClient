package app.rtcmeetings.domain.usecase

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response

interface CreateAccountUseCase {
    fun execute(
        email: String,
        name: String,
        password: String
    ): Single<Response<ResponseBody>>
}