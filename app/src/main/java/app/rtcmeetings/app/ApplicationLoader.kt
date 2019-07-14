package app.rtcmeetings.app

import android.content.Context
import android.content.res.Configuration
import app.rtcmeetings.di.AppComponent
import androidx.lifecycle.ProcessLifecycleOwner
import app.rtcmeetings.di.DaggerAppComponent
import app.rtcmeetings.helper.locale.LocaleManager
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class ApplicationLoader : DaggerApplication() {

    private lateinit var androidInjector: AndroidInjector<out DaggerApplication>

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle
                .addObserver(ApplicationLifecycleObserver(this@ApplicationLoader))
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        INSTANCE = this
        androidInjector = DaggerAppComponent.builder()
            .application(this)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        LocaleManager.setLocale(this)
    }

    companion object {
        private var INSTANCE: ApplicationLoader? = null
        @JvmStatic
        fun get(): ApplicationLoader = INSTANCE!!

        @JvmStatic
        fun getAppComponent(): AppComponent {
            return ApplicationLoader.get().androidInjector as AppComponent
        }
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = androidInjector

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        LocaleManager.setLocale(this)
    }
}