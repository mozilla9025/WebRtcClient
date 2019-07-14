package app.rtcmeetings.domain.respository.impl

import app.rtcmeetings.data.db.dao.ContactDao
import app.rtcmeetings.data.db.dbentity.Contact
import app.rtcmeetings.data.entity.User
import app.rtcmeetings.domain.respository.ContactRepository
import app.rtcmeetings.network.api.ContactsApi
import com.google.gson.Gson
import com.google.gson.JsonArray
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ContactRepositoryImpl @Inject constructor(
    private val contactDao: ContactDao,
    private val contactsApi: ContactsApi,
    private val gson: Gson
) : ContactRepository {

    override fun getContacts(): Observable<List<Contact>> {
        val remote = contactsApi.getContacts()
            .map {
                if (it.isSuccessful) {
                    it.body()?.let { body ->
                        val string = body.string()

                        val result = gson.fromJson<JsonArray>(string, JsonArray::class.java)
                        return@map result.map { element ->
                            gson.fromJson(
                                element.asJsonObject.get("targetAccount"),
                                User::class.java
                            ).toContact()
                        }
                    } ?: throw IllegalStateException("Contact list is not found")
                } else {
                    throw IllegalStateException("Contact list is not found")
                }
            }

        return contactDao.getContacts()
            .flatMapObservable { listFromLocal: List<Contact> ->
                remote.observeOn(Schedulers.computation())
                    .toObservable()
                    .filter { apiUsers: List<Contact> ->
                        apiUsers != listFromLocal
                    }
                    .flatMapSingle { apiContacts: List<Contact> ->
                        contactDao.insert(apiContacts)
                            .andThen(Single.just(apiContacts))
                    }.startWith(listFromLocal)
            }
    }

    override fun removeContact(contact: Contact): Completable {
        return contactDao.delete(contact)
    }

    override fun addContact(contact: Contact): Completable {
        return contactsApi.addContact(contact.id)
            .flatMapCompletable { body ->
                return@flatMapCompletable if (body.isSuccessful) {
                    contactDao.insert(contact)
                } else {
                    throw IllegalStateException("Unable to add contact $contact")
                }
            }
    }

    override fun addContacts(contacts: Collection<Contact>): Completable {
        return contactDao.insert(contacts)
    }
}
