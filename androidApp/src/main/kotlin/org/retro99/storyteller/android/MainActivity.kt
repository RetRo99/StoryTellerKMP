package org.retro99.storyteller.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.retro99.home.ui.deeplink.DeepLinkHandler
import com.retro99.reader.ui.playback.NotificationPermissionHandler
import org.koin.android.ext.android.inject
import org.retro99.storyteller.App

class MainActivity : FragmentActivity() {

    private val notificationPermissionHandler: NotificationPermissionHandler by inject()
    private val deepLinkHandler: DeepLinkHandler by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Register permission handler before setContent
        notificationPermissionHandler.register(this)

        setContent {
            App()
        }

        // Handle deep link if activity was started with one
        handleDeepLinkIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle deep link when activity is already running (singleTask launch mode)
        handleDeepLinkIntent(intent)
    }

    /**
     * Handles deep link intents from notification clicks or external links.
     * Extracts the URI data and passes it to the DeepLinkHandler for navigation.
     */
    private fun handleDeepLinkIntent(intent: Intent?) {
        val uri = intent?.data?.toString()
        if (uri != null) {
            deepLinkHandler.handleDeepLink(uri)
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

