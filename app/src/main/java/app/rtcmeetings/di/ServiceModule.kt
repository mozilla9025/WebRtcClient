package app.rtcmeetings.di

import app.rtcmeetings.network.ws.WsService
import app.rtcmeetings.webrtc.CallService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract fun contributeWsService(): WsService

    @ContributesAndroidInjector
    abstract fun contributeCallService(): CallService

}