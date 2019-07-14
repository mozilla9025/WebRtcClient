package app.rtcmeetings.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import app.rtcmeetings.data.db.dao.ContactDao
import app.rtcmeetings.data.db.dbentity.Contact

@Database(entities = [Contact::class], version = 1, exportSchema = false)
abstract class ApplicationDB : RoomDatabase() {

    abstract fun contactDao(): ContactDao

    companion object {
        const val DB_FILE_NAME = "rtcmeetings.db"
    }
}