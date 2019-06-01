package app.rtcmeetings.di

import app.rtcmeetings.network.CallApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module(includes = [NetworkModule::class])
class ApiModule {
    @Provides
    fun provideCallApi(retrofit: Retrofit): CallApi =
            retrofit.create(CallApi::class.java)
}