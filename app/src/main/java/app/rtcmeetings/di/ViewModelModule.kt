package app.rtcmeetings.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.rtcmeetings.ui.auth.LogInViewModel
import app.rtcmeetings.ui.auth.SignUpViewModel
import app.rtcmeetings.ui.main.MainViewModel
import app.rtcmeetings.ui.start.SplashScreenViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {

    @Binds
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(SignUpViewModel::class)
    fun bindSignUpViewModel(viewModel: SignUpViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LogInViewModel::class)
    fun bindLogInViewModel(viewModel: LogInViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SplashScreenViewModel::class)
    fun bindSplashScreenViewModel(viewModel: SplashScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun bindMainViewModel(viewModel: MainViewModel): ViewModel

}