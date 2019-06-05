package app.rtcmeetings.ui.auth

import androidx.lifecycle.MutableLiveData
import app.rtcmeetings.base.BaseViewModel
import app.rtcmeetings.domain.usecase.LogInUseCase
import app.rtcmeetings.util.loge
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class LogInViewModel @Inject constructor(
        private val loginUseCase: LogInUseCase
) : BaseViewModel() {

    val loginliveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun logIn(email: String, password: String) {
        add(loginUseCase.execute(email, password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ loginliveData.value = true },
                        {
                            loginliveData.value = false
                            loge(it)
                        })
        )
    }
}