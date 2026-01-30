package com.retro99.reader.ui.reader

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.retro99.reader.data.source.PublicationHolder
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubNavigatorFragment

private const val NAVIGATOR_FRAGMENT_TAG = "epub_navigator"

/**
 * Android implementation of EPUB reader using Readium's EpubNavigatorFragment.
 */
@Composable
internal actual fun EpubReaderView(
    bookUuid: String,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    if (activity == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("Error: Activity is not a FragmentActivity")
        }
        return
    }

    val publication = remember(bookUuid) { PublicationHolder.get(bookUuid) }

    if (publication == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("Error: Publication not found for book $bookUuid")
        }
        return
    }

    val navigatorFactory = remember(publication) {
        EpubNavigatorFactory(publication)
    }

    val containerId = remember { View.generateViewId() }

    DisposableEffect(bookUuid) {
        onDispose {
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
            if (fragmentManager.findFragmentByTag(NAVIGATOR_FRAGMENT_TAG) == null) {
                fragmentManager.fragmentFactory = navigatorFactory.createFragmentFactory(
                    initialLocator = null,
                )

                fragmentManager.commit {
                    add(
                        containerView.id,
                        EpubNavigatorFragment::class.java,
                        null,
                        NAVIGATOR_FRAGMENT_TAG,
                    )
                }
            }
        },
    )
}

