package app.rtcmeetings.di

import app.rtcmeetings.domain.usecase.*
import app.rtcmeetings.domain.usecase.impl.*
import dagger.Binds
import dagger.Module

@Module
interface UseCaseModule {

    @Binds
    fun bindCallUseCase(useCase: CallUseCaseImpl): CallUseCase

    @Binds
    fun bindCheckAuthUseCase(useCase: CheckAuthUseCaseImpl): CheckAuthUseCase

    @Binds
    fun bindCreateAccountUseCase(useCase: CreateAccountUseCaseImpl): CreateAccountUseCase

    @Binds
    fun bindLogInUseCase(useCase: LogInUseCaseImpl): LogInUseCase

    @Binds
    fun bindLogOutUseCase(useCase: LogOutUseCaseImpl): LogOutUseCase

    @Binds
    fun bindGetUserUseCase(useCase: GetUserUseCaseImpl): GetUserUseCase

    @Binds
    fun bindContactUseCase(useCase: ContactUseCaseImpl): ContactUseCase

}