package com.retro99.reader.ui.reader

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.ui.controller.AndroidEpubReaderController
import com.retro99.reader.ui.controller.EpubReaderController
import org.koin.compose.koinInject
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.shared.publication.Publication

private const val NAVIGATOR_FRAGMENT_TAG = "epub_navigator"

/**
 * Android implementation of EPUB reader using Readium's EpubNavigatorFragment.
 */
@Composable
internal actual fun EpubReaderView(
    bookUuid: String,
    localFilePath: String,
    settings: ReaderSettingsDomainModel,
    onProgressChanged: (locator: String, progression: Float) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity ?: run {
        ReaderErrorView(message = "Error: Activity is not a FragmentActivity", modifier = modifier)
        return
    }

    val controller: EpubReaderController = koinInject()
    val androidController = controller as AndroidEpubReaderController

    var publication by remember { mutableStateOf<Publication?>(null) }
    var isPublicationReady by remember { mutableStateOf(false) }

    // Open publication when localFilePath is available
    LaunchedEffect(localFilePath) {
        if (localFilePath.isNotEmpty()) {
            val success = androidController.openPublication(localFilePath)
            if (success) {
                publication = androidController.getPublication()
                if (publication != null) {
                    isPublicationReady = true
                }
            }
        }
    }

    // Apply settings when they change and publication is ready
    LaunchedEffect(settings, isPublicationReady) {
        if (isPublicationReady) {
            androidController.setSettings(settings)
        }
    }

    // Show loading or error state if publication is not ready
    val currentPublication = publication
    if (currentPublication == null) {
        // Publication not yet loaded - the LaunchedEffect will handle it
        return
    }

    val navigatorFactory = remember(currentPublication) {
        EpubNavigatorFactory(currentPublication)
    }

    val containerId = remember { View.generateViewId() }

    DisposableEffect(bookUuid) {
        onDispose {
            // Clean up the navigator reference
            androidController.clearNavigator()
            // Clean up the publication
            androidController.closePublication()
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
                fragmentManager.fragmentFactory = navigatorFactory.createFragmentFactory(
                    initialLocator = null,
                    initialPreferences = with(androidController) {
                        settings.toEpubPreferences()
                    },
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

                // Register the navigator with the controller for page navigation
                val navigatorFragment = fragmentManager.findFragmentByTag(NAVIGATOR_FRAGMENT_TAG)
                        as? EpubNavigatorFragment
                navigatorFragment?.let {
                    androidController.setNavigator(it)
                }
            } else {
                // Fragment already exists, ensure it's registered with the controller
                androidController.setNavigator(existingFragment)
            }
        },
    )
}

