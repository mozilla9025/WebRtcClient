package app.rtcmeetings.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.rtcmeetings.ui.module.auth.LogInViewModel
import app.rtcmeetings.ui.module.auth.SignUpViewModel
import app.rtcmeetings.ui.module.contacts.ContactsViewModel
import app.rtcmeetings.ui.module.main.MainViewModel
import app.rtcmeetings.ui.module.p2pcall.P2pCallViewModel
import app.rtcmeetings.ui.module.start.SplashScreenViewModel
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

    @Binds
    @IntoMap
    @ViewModelKey(P2pCallViewModel::class)
    fun bindP2pCallViewModel(viewModel: P2pCallViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactsViewModel::class)
    fun bindContactsViewModel(viewModel: ContactsViewModel): ViewModel

}