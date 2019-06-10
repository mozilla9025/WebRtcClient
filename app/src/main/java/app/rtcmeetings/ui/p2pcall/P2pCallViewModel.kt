package app.rtcmeetings.ui.p2pcall

import androidx.lifecycle.MutableLiveData
import app.rtcmeetings.base.BaseViewModel
import app.rtcmeetings.data.entity.User
import app.rtcmeetings.domain.usecase.GetUserUseCase
import app.rtcmeetings.util.loge
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class P2pCallViewModel @Inject constructor(
        private val getUserUseCase: GetUserUseCase
) : BaseViewModel() {

    val userLiveData: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }

    val currUserLiveData: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }

    fun getUser(id: Int) {
        add(getUserUseCase.getUserById(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ userLiveData.value = it }, {
                    userLiveData.value = null
                    loge(it)
                })
        )
    }

    fun getMe() {
        add(getUserUseCase.getMe()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ currUserLiveData.value = it }, {
                    currUserLiveData.value = null
                    loge(it)
                })
        )
    }

}