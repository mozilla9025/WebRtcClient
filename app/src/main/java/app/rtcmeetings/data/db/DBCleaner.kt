package app.rtcmeetings.data.db

import io.reactivex.Completable

interface DBCleaner {
    fun cleanDatabase() : Completable
}