package app.rtcmeetings.di

import android.content.Context
import android.content.SharedPreferences
import app.rtcmeetings.data.AuthStorage
import app.rtcmeetings.data.PreferencesImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class StorageModule {

    @Singleton
    @Provides
    internal fun providePreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PreferencesImpl.PREFERENCES, Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    internal fun provideAuthStorage(sharedPreferences: SharedPreferences): AuthStorage {
        return PreferencesImpl(sharedPreferences)
    }
}