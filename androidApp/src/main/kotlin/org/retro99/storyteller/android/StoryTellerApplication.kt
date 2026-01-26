package org.retro99.storyteller.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.retro99.storyteller.di.initKoin

class StoryTellerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@StoryTellerApplication)
        }
    }
}

