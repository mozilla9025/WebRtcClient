package app.rtcmeetings.ui.module.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import app.rtcmeetings.base.BaseViewModel
import app.rtcmeetings.data.db.dbentity.Contact
import app.rtcmeetings.domain.usecase.ContactUseCase
import app.rtcmeetings.domain.usecase.LogOutUseCase
import app.rtcmeetings.network.ws.WsService
import app.rtcmeetings.util.logd
import app.rtcmeetings.util.loge
import app.rtcmeetings.util.logi
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val logOutUseCase: LogOutUseCase,
    private val contactUseCase: ContactUseCase
) : BaseViewModel() {

    val logOutLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun startWsService(context: Context) {
        WsService.connect(context)
        add(
            contactUseCase.addContact(Contact(8, "nui", "hui"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { logd("ADDED") },
                    { loge(it) }
                )
        )

        add(
            contactUseCase.getContacts()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ logi("$it") },
                    { loge(it) })
        )
    }

    fun disconnectWs(context: Context) {
        WsService.disconnect(context)
    }

    fun logOut() {
        add(
            logOutUseCase.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ logOutLiveData.value = true }, {
                    logOutLiveData.value = false
                    loge(it)
                })
        )
    }
}