package app.rtcmeetings.di

import android.app.Application
import android.content.Context
import app.rtcmeetings.app.ApplicationLoader
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    includes = [
        DatabaseModule::class,
        NetworkModule::class,
        ApiModule::class,
        UseCaseModule::class,
        RepositoryModule::class]
)
internal class AppModule {

    @Singleton
    @Provides
    fun provideApplication(application: Application): ApplicationLoader = application as ApplicationLoader

    @Singleton
    @Provides
    fun provideApplicationContext(application: Application): Context = application.applicationContext

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }
}