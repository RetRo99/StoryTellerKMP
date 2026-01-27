package org.retro99.storyteller.di

import com.retro99.analytics.implementation.di.AnalyticsModule
import com.retro99.auth.data.di.AuthDataModule
import com.retro99.auth.domain.di.AuthDomainModule
import com.retro99.base.buildconfig.di.BuildConfigModule
import com.retro99.home.data.di.HomeDataModule
import com.retro99.home.ui.di.HomeUiModule
import com.retro99.login.data.di.LoginDataModule
import com.retro99.login.domain.di.LoginDomainModule
import com.retro99.login.ui.di.LoginUiModule
import com.retro99.network.implementation.di.NetworkingModule
import com.retro99.preferences.implementation.di.PreferencesModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(
    includes = [
        AnalyticsModule::class,
        BuildConfigModule::class,
        NetworkingModule::class,
        PreferencesModule::class,
        AuthDomainModule::class,
        AuthDataModule::class,
        LoginUiModule::class,
        LoginDomainModule::class,
        LoginDataModule::class,
        HomeUiModule::class,
        HomeDataModule::class,
    ],
)
@Configuration
@ComponentScan("org.retro99.storyteller")
class AppModule

