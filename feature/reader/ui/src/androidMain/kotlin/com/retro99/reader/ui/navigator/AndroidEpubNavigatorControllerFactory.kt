package com.retro99.reader.ui.navigator

import android.content.Context
import com.retro99.analytics.api.Analytics
import com.retro99.reader.ui.media.smil.SmilParser
import com.retro99.reader.ui.publication.EpubPublication
import org.koin.core.annotation.Single
import org.readium.r2.navigator.epub.EpubNavigatorFragment

/**
 * Factory for creating [AndroidEpubNavigatorController] instances.
 *
 * This factory is injected via Koin and holds dependencies that are available at DI time
 * (like [Analytics] and [SmilParser]). The [create] method accepts runtime dependencies
 * that are only available when the navigator fragment is ready.
 *
 * @param analytics Analytics instance for logging errors and events
 * @param smilParser Parser for SMIL media overlay files
 */
@Single
class AndroidEpubNavigatorControllerFactory(
    private val analytics: Analytics,
    private val smilParser: SmilParser,
) {

    /**
     * Creates a new [AndroidEpubNavigatorController] instance.
     *
     * @param navigator The Readium EpubNavigatorFragment to wrap
     * @param publication The opened EPUB publication
     * @param context Android context for creating the media player
     * @return A new controller instance
     */
    fun create(
        navigator: EpubNavigatorFragment,
        publication: EpubPublication,
        context: Context,
    ): AndroidEpubNavigatorController {
        return AndroidEpubNavigatorController(
            navigator = navigator,
            publication = publication,
            context = context,
            analytics = analytics,
            smilParser = smilParser,
        )
    }
}

