package app.rtcmeetings.di

import app.rtcmeetings.ui.module.auth.LogInFragment
import app.rtcmeetings.ui.module.auth.SignUpFragment
import app.rtcmeetings.ui.module.main.MainFragment
import app.rtcmeetings.ui.module.p2pcall.P2pCallFragment
import app.rtcmeetings.ui.module.start.SplashScreenFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeSignUpFragment(): SignUpFragment

    @ContributesAndroidInjector
    abstract fun contributeLogInFragment(): LogInFragment

    @ContributesAndroidInjector
    abstract fun contributeSplashScreen(): SplashScreenFragment

    @ContributesAndroidInjector
    abstract fun contributeMainFragment(): MainFragment

    @ContributesAndroidInjector
    abstract fun contributeP2pCallFragment(): P2pCallFragment
}