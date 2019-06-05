package app.rtcmeetings.di

import app.rtcmeetings.domain.respository.AuthRepository
import app.rtcmeetings.domain.respository.impl.AuthRepositoryImpl
import dagger.Binds
import dagger.Module

@Module(includes = [StorageModule::class])
interface RepositoryModule {

    @Binds
    fun bindAuthRepository(repository: AuthRepositoryImpl): AuthRepository
}