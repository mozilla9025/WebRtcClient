package app.rtcmeetings.data.db.dao

import androidx.room.*
import app.rtcmeetings.data.db.dbentity.Contact
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface ContactDao {

    @Query("SELECT * FROM $TABLE_NAME")
    fun getContacts(): Single<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contact: Contact): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contacts: Collection<Contact>): Completable

    @Delete
    fun delete(contact: Contact): Completable

    @Query("DELETE FROM $TABLE_NAME")
    fun clean(): Completable

    companion object {
        const val TABLE_NAME = "contact"
    }
}