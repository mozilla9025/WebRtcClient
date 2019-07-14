package app.rtcmeetings.domain.respository

import app.rtcmeetings.data.db.dbentity.Contact
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable

interface ContactRepository {

    fun getContacts(): Observable<List<Contact>>

    fun removeContact(contact: Contact): Completable

    fun addContact(contact: Contact): Completable

    fun addContacts(contacts: Collection<Contact>): Completable
}