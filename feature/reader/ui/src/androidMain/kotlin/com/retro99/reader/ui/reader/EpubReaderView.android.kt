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
import com.retro99.reader.ui.navigator.AndroidEpubNavigatorController
import com.retro99.reader.ui.navigator.EpubNavigatorController
import com.retro99.reader.ui.navigator.toEpubPreferences
import com.retro99.reader.ui.service.AndroidEpubPublicationService
import com.retro99.reader.ui.service.EpubPublicationService
import kotlinx.coroutines.flow.Flow
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
    commands: Flow<ReaderCommand>,
    publicationService: EpubPublicationService,
    onProgressChanged: (locator: String, progression: Float) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity ?: run {
        ReaderErrorView(message = "Error: Activity is not a FragmentActivity", modifier = modifier)
        return
    }

    val androidService = publicationService as AndroidEpubPublicationService

    var publication by remember { mutableStateOf<Publication?>(null) }
    var isPublicationReady by remember { mutableStateOf(false) }
    var navigatorController by remember { mutableStateOf<EpubNavigatorController?>(null) }

    // Open publication when localFilePath is available
    LaunchedEffect(localFilePath) {
        if (localFilePath.isNotEmpty()) {
            val success = androidService.openPublication(localFilePath)
            if (success) {
                publication = androidService.getPublication()
                if (publication != null) {
                    isPublicationReady = true
                }
            }
        }
    }

    // Collect commands and execute on navigator controller
    LaunchedEffect(navigatorController) {
        navigatorController?.let { controller ->
            commands.collect { command ->
                when (command) {
                    is ReaderCommand.GoToNextPage -> controller.goToNextPage()
                    is ReaderCommand.GoToPreviousPage -> controller.goToPreviousPage()
                    is ReaderCommand.GoToChapter -> controller.goToChapter(command.href)
                    is ReaderCommand.ApplySettings -> controller.setSettings(command.settings)
                }
            }
        }
    }

    // Apply settings when they change and navigator is ready
    LaunchedEffect(settings, navigatorController) {
        navigatorController?.setSettings(settings)
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
            // Clean up the navigator controller
            navigatorController = null
            // Clean up the publication
            androidService.closePublication()
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
                    initialPreferences = settings.toEpubPreferences(),
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

