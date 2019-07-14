package app.rtcmeetings.di

import app.rtcmeetings.ui.module.CallActivity
import app.rtcmeetings.ui.module.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [FragmentModule::class])
abstract class ActivityModule {
    @ContributesAndroidInjector
    internal abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    internal abstract fun contributeCallActivity(): CallActivity
}