package app.rtcmeetings.util.rxbus

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object RxBus {
    private val publisher: PublishSubject<Any> = PublishSubject.create()

    fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)

    fun post(event: Any) {
        publisher.onNext(event)
    }
}