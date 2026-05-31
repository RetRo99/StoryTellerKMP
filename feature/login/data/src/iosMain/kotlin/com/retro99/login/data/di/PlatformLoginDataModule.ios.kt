package com.retro99.login.data.di

import com.retro99.login.data.oauth.IosStorytellerOAuthSessionLauncher
import com.retro99.login.data.oauth.StorytellerOAuthSessionLauncher
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
actual class PlatformLoginDataModule {

    @Single
    fun provideStorytellerOAuthSessionLauncher(): StorytellerOAuthSessionLauncher {
        return IosStorytellerOAuthSessionLauncher()
    }
}
