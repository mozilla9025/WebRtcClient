package app.rtcmeetings.ui.main

import androidx.lifecycle.MutableLiveData
import app.rtcmeetings.base.BaseViewModel
import app.rtcmeetings.data.entity.User
import app.rtcmeetings.domain.usecase.CallUseCase
import app.rtcmeetings.domain.usecase.GetUserUseCase
import app.rtcmeetings.domain.usecase.LogOutUseCase
import app.rtcmeetings.util.loge
import app.rtcmeetings.util.logi
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class MainViewModel @Inject constructor(
        private val logOutUseCase: LogOutUseCase,
        private val getUserUseCase: GetUserUseCase,
        private val callUseCase: CallUseCase
) : BaseViewModel() {

    val logOutLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val userLiveData: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }

    fun getUser(id: Int) {
        add(getUserUseCase.getUserById(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ userLiveData.value = it }, {
                    userLiveData.value = null
                    loge(it)
                }))
    }

    fun logOut() {
        add(logOutUseCase.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ logOutLiveData.value = true }, {
                    logOutLiveData.value = false
                    loge(it)
                }))
    }
}