package app.rtcmeetings.ui.auth

import androidx.lifecycle.MutableLiveData
import app.rtcmeetings.base.BaseViewModel
import app.rtcmeetings.domain.usecase.CreateAccountUseCase
import app.rtcmeetings.util.loge
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class SignUpViewModel @Inject constructor(
    private val createAccountUseCase: CreateAccountUseCase
) : BaseViewModel() {

    val signUpLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun signUp(email: String, name: String, password: String) {
        add(
            createAccountUseCase.execute(email, name, password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ signUpLiveData.value = true }, {
                    signUpLiveData.value = false
                    loge(it)
                })
        )
    }
}