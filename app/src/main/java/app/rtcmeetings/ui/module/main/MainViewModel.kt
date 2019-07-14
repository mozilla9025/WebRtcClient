package app.rtcmeetings.ui.module.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import app.rtcmeetings.base.BaseViewModel
import app.rtcmeetings.domain.usecase.LogOutUseCase
import app.rtcmeetings.network.ws.WsService
import app.rtcmeetings.util.loge
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class MainViewModel @Inject constructor(
        private val logOutUseCase: LogOutUseCase
) : BaseViewModel() {

    val logOutLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun startWsService(context: Context) {
        WsService.connect(context)
    }

    fun disconnectWs(context: Context) {
        WsService.disconnect(context)
    }

    fun logOut() {
        add(logOutUseCase.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ logOutLiveData.value = true }, {
                    logOutLiveData.value = false
                    loge(it)
                })
        )
    }
}