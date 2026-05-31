package com.retro99.login.data.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(
    includes = [
        PlatformLoginDataModule::class,
    ],
)
@Configuration
@ComponentScan("com.retro99.login.data")
class LoginDataModule

