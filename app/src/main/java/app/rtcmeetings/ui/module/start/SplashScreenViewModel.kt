package app.rtcmeetings.ui.module.start

import androidx.lifecycle.MutableLiveData
import app.rtcmeetings.base.BaseViewModel
import app.rtcmeetings.domain.usecase.CheckAuthUseCase
import app.rtcmeetings.util.loge
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class SplashScreenViewModel @Inject constructor(
        private val checkAuthUseCase: CheckAuthUseCase
) : BaseViewModel() {

    val authLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun checkIfAuthorized() {
        add(checkAuthUseCase.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { authLiveData.value = it },
                        {
                            authLiveData.value = false
                            loge(it)
                        })
        )
    }
}