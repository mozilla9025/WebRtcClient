package app.rtcmeetings.di

import app.rtcmeetings.domain.usecase.CallUseCase
import app.rtcmeetings.domain.usecase.CheckAuthUseCase
import app.rtcmeetings.domain.usecase.CreateAccountUseCase
import app.rtcmeetings.domain.usecase.LogInUseCase
import app.rtcmeetings.domain.usecase.impl.CallUseCaseImpl
import app.rtcmeetings.domain.usecase.impl.CheckAuthUseCaseImpl
import app.rtcmeetings.domain.usecase.impl.CreateAccountUseCaseImpl
import app.rtcmeetings.domain.usecase.impl.LogInUseCaseImpl
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

}