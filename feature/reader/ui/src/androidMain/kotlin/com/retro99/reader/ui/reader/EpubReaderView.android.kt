package com.retro99.reader.ui.reader

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.retro99.reader.ui.navigator.AndroidEpubNavigatorController
import com.retro99.reader.ui.navigator.EpubNavigatorController
import com.retro99.reader.ui.navigator.toEpubPreferences
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.coroutines.flow.Flow
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType

private const val NAVIGATOR_FRAGMENT_TAG = "epub_navigator"

/**
 * Android implementation of EPUB reader using Readium's EpubNavigatorFragment.
 */
@Composable
internal actual fun EpubReaderView(
    bookUuid: String,
    publication: EpubPublication,
    commands: Flow<ReaderCommand>,
    onProgressChanged: (locator: String, progression: Float) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity ?: run {
        ReaderErrorView(message = "Error: Activity is not a FragmentActivity", modifier = modifier)
        return
    }

    val readiumPublication = publication.publication

    var navigatorController by remember { mutableStateOf<EpubNavigatorController?>(null) }

    // Use common command handling logic
    HandleNavigatorCommands(
        navigator = navigatorController,
        commands = commands,
    )

    val navigatorFactory = remember(readiumPublication) {
        EpubNavigatorFactory(readiumPublication)
    }

    val containerId = remember { View.generateViewId() }

    DisposableEffect(bookUuid) {
        onDispose {
            // Clean up the navigator controller
            navigatorController = null
            // Clean up the publication
            publication.close()
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
                val initialLocator = publication.initialLocator?.let { locator ->
                    Url(locator.href)?.let { url ->
                        Locator(
                            href = url,
                            mediaType = MediaType(locator.type) ?: MediaType.XHTML,
                            title = null,
                            locations = Locator.Locations(
                                progression = locator.progression,
                                position = locator.position,
                                totalProgression = locator.totalProgression,
                            ),
                        )
                    }
                }
                fragmentManager.fragmentFactory = navigatorFactory.createFragmentFactory(
                    initialLocator = initialLocator,
                    initialPreferences = publication.initialSettings.toEpubPreferences(),
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

                // Create navigator controller from the fragment
                val navigatorFragment = fragmentManager.findFragmentByTag(NAVIGATOR_FRAGMENT_TAG)
                        as? EpubNavigatorFragment
                navigatorFragment?.let {
                    navigatorController = AndroidEpubNavigatorController(it)
                }
            } else {
                // Fragment already exists, ensure controller is created
                if (navigatorController == null) {
                    navigatorController = AndroidEpubNavigatorController(existingFragment)
                }
            }
        },
    )
}

