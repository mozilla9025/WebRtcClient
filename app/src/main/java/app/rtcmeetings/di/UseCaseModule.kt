package app.rtcmeetings.di

import app.rtcmeetings.domain.usecase.CallUseCase
import app.rtcmeetings.domain.usecase.CheckAuthUseCase
import app.rtcmeetings.domain.usecase.impl.CallUseCaseImpl
import app.rtcmeetings.domain.usecase.impl.CheckAuthUseCaseImpl
import dagger.Binds
import dagger.Module

@Module
interface UseCaseModule {

    @Binds
    fun bindCallUseCase(useCase: CallUseCaseImpl): CallUseCase

    @Binds
    fun bindCheckAuthUseCase(useCase: CheckAuthUseCaseImpl): CheckAuthUseCase
}