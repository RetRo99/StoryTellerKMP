package org.retro99.storyteller.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.retro99.reader.ui.playback.NotificationPermissionHandler
import org.koin.android.ext.android.inject
import org.retro99.storyteller.App

class MainActivity : FragmentActivity() {

    private val notificationPermissionHandler: NotificationPermissionHandler by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Register permission handler before setContent
        notificationPermissionHandler.register(this)

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Only unregister when truly finishing, not during config changes
        // The handler survives config changes and will be re-registered in onCreate
        if (isFinishing) {
            notificationPermissionHandler.unregister()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

