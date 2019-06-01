package app.rtcmeetings.di

import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module

@Module
interface ViewModelModule {

    @Binds
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
//
//    @Binds
//    @IntoMap
//    @ViewModelKey(HomeViewModel::class)
//    fun bindHomeViewModel(viewModel: HomeViewModel): ViewModel
}