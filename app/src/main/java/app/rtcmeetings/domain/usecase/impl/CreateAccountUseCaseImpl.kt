package app.rtcmeetings.domain.usecase.impl

import app.rtcmeetings.domain.usecase.CreateAccountUseCase
import app.rtcmeetings.network.api.AuthApi
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class CreateAccountUseCaseImpl @Inject constructor(
    private val api: AuthApi
) : CreateAccountUseCase {

    override fun execute(
        email: String,
        name: String,
        password: String
    ): Single<Response<ResponseBody>> {
        return api.createAccount(email, name, password)
            .subscribeOn(Schedulers.io())
    }
}