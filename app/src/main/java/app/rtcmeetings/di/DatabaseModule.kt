package app.rtcmeetings.di

import androidx.room.Room
import app.rtcmeetings.app.ApplicationLoader
import app.rtcmeetings.data.db.ApplicationDB
import app.rtcmeetings.data.db.DBCleaner
import app.rtcmeetings.data.db.dao.ContactDao
import dagger.Module
import dagger.Provides
import io.reactivex.Completable
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: ApplicationLoader) = Room.databaseBuilder(
            app, ApplicationDB::class.java, ApplicationDB.DB_FILE_NAME
    )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun providesDbCleaner(db: ApplicationDB): DBCleaner {
        return object : DBCleaner {
            override fun cleanDatabase(): Completable {
                return Completable.fromAction {
                    db.clearAllTables()
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideContactDao(db: ApplicationDB): ContactDao = db.contactDao()
}