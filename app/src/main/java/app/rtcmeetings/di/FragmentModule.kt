package app.rtcmeetings.di

import app.rtcmeetings.ui.auth.LogInFragment
import app.rtcmeetings.ui.auth.SignUpFragment
import app.rtcmeetings.ui.main.MainFragment
import app.rtcmeetings.ui.p2pcall.P2pCallFragment
import app.rtcmeetings.ui.start.SplashScreenFragment
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