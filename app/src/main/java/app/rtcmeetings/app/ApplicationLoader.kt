package app.rtcmeetings.app

import android.content.Context
import app.rtcmeetings.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class ApplicationLoader : DaggerApplication() {

    private lateinit var injector: AndroidInjector<out DaggerApplication>

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        injector = DaggerAppComponent.builder().apply {
            application(this@ApplicationLoader)
        }.build()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = injector
}