package app.rtcmeetings.di

import android.app.Application
import app.rtcmeetings.BuildConfig
import app.rtcmeetings.data.AuthStorage
import app.rtcmeetings.network.AccessTokenManager
import app.rtcmeetings.network.HeaderInterceptor
import app.rtcmeetings.util.l
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    @Singleton
    fun provideHttpCache(application: Application): Cache {
        val cacheSize = 10 * 1024 * 1024
        return Cache(application.cacheDir, cacheSize.l)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(cache: Cache, headerInterceptor: HeaderInterceptor): OkHttpClient =
        OkHttpClient.Builder().apply {
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(1, TimeUnit.MINUTES)
            writeTimeout(1, TimeUnit.MINUTES)
            cache(cache)
            addInterceptor(HttpLoggingInterceptor()
                .apply { level = HttpLoggingInterceptor.Level.BODY })
            addInterceptor(headerInterceptor)
        }.build()

    @Provides
    @Singleton
    fun provideHeaderInterceptor(accessTokenManager: AccessTokenManager): HeaderInterceptor {
        return HeaderInterceptor(accessTokenManager.getAccessToken())
    }

    @Provides
    @Singleton
    fun provideAccessTokenManager(authStorage: AuthStorage): AccessTokenManager {
        return AccessTokenManager(authStorage)
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder().apply {
            baseUrl(BuildConfig.BASE_URL)
            addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            addConverterFactory(GsonConverterFactory.create())
            client(client)
        }.build()
}
