package com.retro99.reader.ui.reader

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import com.retro99.base.ui.IntentDispatcher
import com.retro99.reader.ui.navigator.AndroidBookController
import com.retro99.reader.ui.navigator.BookController
import com.retro99.reader.ui.navigator.DoubleTapJsInterface
import com.retro99.reader.ui.navigator.toAndroidLocator
import com.retro99.reader.ui.navigator.toEpubPreferences
import com.retro99.reader.ui.publication.EpubPublication
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubNavigatorFragment

private const val NAVIGATOR_FRAGMENT_TAG = "epub_navigator"

/**
 * Android implementation of EPUB reader using Readium's EpubNavigatorFragment.
 */
@Composable
internal actual fun EpubReaderViewInternal(
    bookUuid: String,
    publication: EpubPublication,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    bookController: BookController,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity ?: run {
        ReaderErrorView(message = "Error: Activity is not a FragmentActivity", modifier = modifier)
        return
    }

    val readiumPublication = publication.publication

    val navigatorController = bookController as? AndroidBookController

    // Observe permission denied dialog state
//    val showPermissionDeniedDialog by navigatorController?.showPermissionDeniedDialog
//        ?.collectAsState(initial = false)
//        ?: remember { mutableStateOf(false) }
//    val showPermissionRationale by navigatorController?.showPermissionRationale
//        ?.collectAsState(initial = false)
//        ?: remember { mutableStateOf(false) }
//    val openAppSettings = rememberOpenAppSettings()

//    ObservePermissionDeniedDialog(
//        navigator = navigatorController,
//        showDialog = showPermissionDeniedDialog,
//        showRationale = showPermissionRationale,
//        onOpenSettings = openAppSettings,
//        onTryAgain = {
//            // User wants to try again - trigger play which will re-request permission
//            navigatorController?.playAudio()
//        },
//        onDismiss = { /* Dialog dismissed without opening settings */ },
//    )

    val navigatorFactory = remember(readiumPublication) {
        EpubNavigatorFactory(readiumPublication)
    }

    // Create navigator configuration with JavaScript interface for double-tap detection
    val navigatorConfiguration = remember(navigatorController) {
        EpubNavigatorFragment.Configuration {
            // Register JavaScript interface for double-tap detection on sentences
            navigatorController?.let { controller ->
                registerJavascriptInterface("SentenceDoubleTap") { _ ->
                    DoubleTapJsInterface(controller::onSentenceDoubleTap)
                }
            }
        }
    }

    val containerId = remember { View.generateViewId() }

    DisposableEffect(bookUuid) {
        onDispose {
            // Release media overlay player resources
            navigatorController?.close()
            // Clean up the navigator controller
            // Clean up the fragment when the composable is disposed
            val existingFragment = activity.supportFragmentManager
                .findFragmentByTag(NAVIGATOR_FRAGMENT_TAG)
            if (existingFragment != null) {
                activity.supportFragmentManager.commit {
                    remove(existingFragment)
                }
            }
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            FragmentContainerView(ctx).apply {
                id = containerId
            }
        },
        update = { containerView ->
            val fragmentManager = activity.supportFragmentManager

            // Only add fragment if it doesn't exist
            val existingFragment = fragmentManager.findFragmentByTag(NAVIGATOR_FRAGMENT_TAG)
                    as? EpubNavigatorFragment
            if (existingFragment == null) {
                val initialLocator = publication.initialPosition?.toAndroidLocator()
                fragmentManager.fragmentFactory = navigatorFactory.createFragmentFactory(
                    initialLocator = initialLocator,
                    initialPreferences = publication.initialSettings.toEpubPreferences(),
                    configuration = navigatorConfiguration,
                )

                // Use commitNow to make the transaction synchronous
                // This ensures the fragment is immediately available after this call
                fragmentManager.commitNow {
                    add(
                        containerView.id,
                        EpubNavigatorFragment::class.java,
                        null,
                        NAVIGATOR_FRAGMENT_TAG,
                    )
                }
            }

            // Create navigator controller if needed
            val navigatorFragment = (existingFragment
                ?: fragmentManager.findFragmentByTag(NAVIGATOR_FRAGMENT_TAG))
                    as? EpubNavigatorFragment

            navigatorFragment?.let {
                navigatorController?.init(
                    navigator = it,
                    hasMediaOverlays = publication.hasMediaOverlays,
                )
            }
        },
    )
}
