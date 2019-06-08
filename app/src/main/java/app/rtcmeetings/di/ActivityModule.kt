package app.rtcmeetings.di

import app.rtcmeetings.ui.CallActivity
import app.rtcmeetings.ui.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [FragmentModule::class])
abstract class ActivityModule {
    @ContributesAndroidInjector
    internal abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    internal abstract fun contributeCallActivity(): CallActivity
}