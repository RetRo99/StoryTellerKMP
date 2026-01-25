package org.retro99.storyteller.android

import android.app.Application
import io.kotzilla.sdk.analytics.koin.analytics
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.ksp.generated.startKoin
import org.retro99.storyteller.di.StoryTellerKoinApp

class StoryTellerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        StoryTellerKoinApp().startKoin {
            androidLogger()
            androidContext(this@StoryTellerApplication)
            analytics()
        }
    }
}

