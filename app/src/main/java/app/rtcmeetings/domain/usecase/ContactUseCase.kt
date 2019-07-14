package app.rtcmeetings.domain.usecase

import app.rtcmeetings.data.db.dbentity.Contact
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable

interface ContactUseCase {
    fun getContacts(): Observable<List<Contact>>
    fun addContact(contact: Contact): Completable
    fun removeContact(contact: Contact): Completable
    fun addContacts(contacts: Collection<Contact>): Completable
}