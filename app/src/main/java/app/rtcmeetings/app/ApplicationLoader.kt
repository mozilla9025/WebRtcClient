package app.rtcmeetings.app

import app.rtcmeetings.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class ApplicationLoader : DaggerApplication() {

    private lateinit var injector: AndroidInjector<out DaggerApplication>

    override fun onCreate() {
        super.onCreate()
        injector = DaggerAppComponent.builder()
            .application(this)
            .build()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = injector
}