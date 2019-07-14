package app.rtcmeetings.domain.usecase.impl

import app.rtcmeetings.data.db.dbentity.Contact
import app.rtcmeetings.domain.respository.ContactRepository
import app.rtcmeetings.domain.usecase.ContactUseCase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ContactUseCaseImpl @Inject constructor(
    private val contactRepository: ContactRepository
) : ContactUseCase {
    override fun getContacts(): Observable<List<Contact>> {
        return contactRepository.getContacts()
            .subscribeOn(Schedulers.io())
    }

    override fun addContact(contact: Contact): Completable {
        return contactRepository.addContact(contact)
            .subscribeOn(Schedulers.io())
    }

    override fun removeContact(contact: Contact): Completable {
        return contactRepository.removeContact(contact)
            .subscribeOn(Schedulers.io())
    }

    override fun addContacts(contacts: Collection<Contact>): Completable {
        return contactRepository.addContacts(contacts)
            .subscribeOn(Schedulers.io())
    }
}