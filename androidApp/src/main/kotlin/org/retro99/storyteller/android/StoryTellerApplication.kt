package org.retro99.storyteller.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.retro99.storyteller.di.platformModules

class StoryTellerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@StoryTellerApplication)
            modules(platformModules())
        }
    }
}

