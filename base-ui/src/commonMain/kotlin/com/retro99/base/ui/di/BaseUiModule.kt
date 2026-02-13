package com.retro99.base.ui.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(
    includes = [
        PlatformBaseUiModule::class,
    ],
)
@Configuration
@ComponentScan("com.retro99.base.ui")
class BaseUiModule

