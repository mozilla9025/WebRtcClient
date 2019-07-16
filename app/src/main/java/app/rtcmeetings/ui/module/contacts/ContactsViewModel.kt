package app.rtcmeetings.ui.module.contacts

import androidx.lifecycle.MutableLiveData
import app.rtcmeetings.base.BaseViewModel
import app.rtcmeetings.data.db.dbentity.Contact
import app.rtcmeetings.data.entity.User
import app.rtcmeetings.domain.usecase.ContactUseCase
import app.rtcmeetings.domain.usecase.GetUserUseCase
import app.rtcmeetings.network.result.Response
import app.rtcmeetings.util.loge
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class ContactsViewModel @Inject constructor(
        private val contactUseCase: ContactUseCase,
        private val getUserUseCase: GetUserUseCase
) : BaseViewModel() {

    internal val getContactsLiveData: MutableLiveData<Response<List<Contact>>> by lazy {
        MutableLiveData<Response<List<Contact>>>()
    }
    internal val addContactsLiveData: MutableLiveData<Response<Unit>> by lazy {
        MutableLiveData<Response<Unit>>()
    }
    internal val userLiveData: MutableLiveData<Response<User>> by lazy {
        MutableLiveData<Response<User>>()
    }

    internal fun findUser(email: String) {
        add(getUserUseCase.getUserByEmail(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ userLiveData.value = Response.success(it) }, {
                    userLiveData.value = Response.failure(it)
                    loge(it)
                })
        )
    }

    internal fun getContacts() {
        add(contactUseCase.getContacts()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { getContactsLiveData.value = Response.loading() }
                .subscribe({ getContactsLiveData.value = Response.success(it) },
                        {
                            getContactsLiveData.value = Response.failure(it)
                            loge(it)
                        })
        )
    }

    internal fun addContact(user: User) {
        add(contactUseCase.addContact(user.toContact())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { addContactsLiveData.value = Response.loading() }
                .subscribe({ addContactsLiveData.value = Response.success(null) },
                        {
                            addContactsLiveData.value = Response.failure(it)
                            loge(it)
                        })
        )
    }

}