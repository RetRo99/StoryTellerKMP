package com.retro99.parrot.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.retro99.home.ui.deeplink.DeepLinkHandler
import com.retro99.parrot.App
import com.retro99.reader.ui.fragment.EpubFragmentFactoryHelper
import com.retro99.reader.ui.playback.NotificationPermissionHandler
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {

    private val notificationPermissionHandler: NotificationPermissionHandler by inject()
    private val deepLinkHandler: DeepLinkHandler by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set a dummy fragment factory BEFORE super.onCreate() to prevent crashes when
        // Android tries to restore EpubNavigatorFragment after process death.
        // EpubNavigatorFragment requires factory instantiation (no default constructor),
        // so without this, the app would crash with Fragment$InstantiationException.
        // This is the official Readium approach used in their test app.
        supportFragmentManager.fragmentFactory = EpubFragmentFactoryHelper.createDummyFactory()

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Remove any restored EpubNavigatorFragment BEFORE onResume is called.
        // The dummy fragment throws RestorationNotSupportedException in onResume,
        // so we must remove it immediately after restoration.
        EpubFragmentFactoryHelper.removeRestoredFragment(supportFragmentManager)

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

    override fun onSaveInstanceState(outState: Bundle) {
        // Readium's EpubNavigatorFragment cannot be restored after process death.
        // Remove it before FragmentActivity saves fragment state so Android never
        // resurrects the unsupported dummy fragment into onResume().
        EpubFragmentFactoryHelper.removeRestoredFragment(supportFragmentManager)
        super.onSaveInstanceState(outState)
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

