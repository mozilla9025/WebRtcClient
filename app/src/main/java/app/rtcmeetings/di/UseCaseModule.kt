package app.rtcmeetings.di

import app.rtcmeetings.domain.CallUseCase
import app.rtcmeetings.domain.usecase.impl.CallUseCaseImpl
import dagger.Binds
import dagger.Module

@Module
interface UseCaseModule {

    @Binds
    fun bindCallUseCase(useCase: CallUseCaseImpl): CallUseCase
}