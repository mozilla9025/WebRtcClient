package app.rtcmeetings.di

import app.rtcmeetings.network.api.UserApi
import app.rtcmeetings.network.api.AuthApi
import app.rtcmeetings.network.api.CallApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module(includes = [NetworkModule::class])
class ApiModule {
    @Provides
    fun provideCallApi(retrofit: Retrofit): CallApi =
            retrofit.create(CallApi::class.java)

    @Provides
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
            retrofit.create(AuthApi::class.java)

    @Provides
    fun provideAccountApi(retrofit: Retrofit): UserApi =
            retrofit.create(UserApi::class.java)
}