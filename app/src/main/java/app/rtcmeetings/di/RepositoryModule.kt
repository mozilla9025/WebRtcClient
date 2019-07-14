package app.rtcmeetings.di

import app.rtcmeetings.domain.respository.AuthRepository
import app.rtcmeetings.domain.respository.CallRepository
import app.rtcmeetings.domain.respository.ContactRepository
import app.rtcmeetings.domain.respository.UserRepository
import app.rtcmeetings.domain.respository.impl.AuthRepositoryImpl
import app.rtcmeetings.domain.respository.impl.CallRepositoryImpl
import app.rtcmeetings.domain.respository.impl.ContactRepositoryImpl
import app.rtcmeetings.domain.respository.impl.UserRepositoryImpl
import dagger.Binds
import dagger.Module

@Module(includes = [StorageModule::class])
interface RepositoryModule {

    @Binds
    fun bindAuthRepository(repository: AuthRepositoryImpl): AuthRepository

    @Binds
    fun bindUserRepository(repository: UserRepositoryImpl): UserRepository

    @Binds
    fun bindCallRepository(repository: CallRepositoryImpl): CallRepository

    @Binds
    fun bindContactRepository(repository: ContactRepositoryImpl): ContactRepository
}