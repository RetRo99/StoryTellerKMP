package org.retro99.storyteller.di

import com.retro99.base.buildconfig.di.BuildConfigModule
import com.retro99.home.data.di.HomeDataModule
import com.retro99.home.ui.di.HomeUiModule
import com.retro99.login.data.di.LoginDataModule
import com.retro99.login.ui.di.LoginUiModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(
    includes = [
        BuildConfigModule::class,
        // NetworkingModule excluded - has pre-existing compilation errors (missing Analytics dependency)
        LoginDataModule::class,
        LoginUiModule::class,
        HomeDataModule::class,
        HomeUiModule::class,
    ]
)
@ComponentScan("org.retro99.storyteller")
class AppModule

