package app.rtcmeetings.base

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseViewModel : ViewModel() {

    private val disposables: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    protected fun add(disposable: Disposable) {
        disposables.add(disposable)
    }

    open fun clear() {
        disposables.clear()
    }

    final override fun onCleared() {
        super.onCleared()
        clear()
    }
}