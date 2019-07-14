package app.rtcmeetings.app

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import app.rtcmeetings.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class ApplicationLoader : DaggerApplication() {

    private lateinit var injector: AndroidInjector<out DaggerApplication>

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle
                .addObserver(ApplicationLifecycleObserver(this@ApplicationLoader))
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        injector = DaggerAppComponent.builder().apply {
            application(this@ApplicationLoader)
        }.build()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = injector
}