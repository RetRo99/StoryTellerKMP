package com.retro99.reader.ui.reader

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.retro99.base.ui.IntentDispatcher
import com.retro99.reader.ui.navigator.AndroidBookController
import com.retro99.reader.ui.navigator.BookController
import com.retro99.reader.ui.navigator.DoubleTapJsInterface
import com.retro99.reader.ui.navigator.toAndroidLocator
import com.retro99.reader.ui.navigator.toEpubPreferences
import com.retro99.reader.ui.publication.EpubPublication
import org.readium.r2.navigator.Decoration
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.html.HtmlDecorationTemplate
import org.readium.r2.navigator.html.HtmlDecorationTemplates
import org.readium.r2.navigator.html.toCss

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
    // Use custom decoration templates that respect the user's alpha choice from the color picker
    // (Readium's default templates override alpha with 0.3, ignoring the user's selection)
    val navigatorConfiguration = remember(navigatorController) {
        EpubNavigatorFragment.Configuration(
            decorationTemplates = createUserAlphaDecorationTemplates()
        ).apply {
            // Register JavaScript interface for double-tap detection on sentences
            navigatorController?.let { controller ->
                registerJavascriptInterface("SentenceDoubleTap") { _ ->
                    DoubleTapJsInterface(controller::onSentenceDoubleTap)
                }
            }
        }
    }

    val containerId = remember { View.generateViewId() }

    val lifecycleOwner = LocalLifecycleOwner.current

    // Use lifecycle observer to properly clean up the fragment.
    // We remove the fragment during ON_DESTROY to clean up resources while keeping
    // the fragment visible during lock/unlock (ON_STOP/ON_START cycles).
    DisposableEffect(bookUuid, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                // Release media overlay player resources
                navigatorController?.close()
                val existingFragment = activity.supportFragmentManager
                    .findFragmentByTag(NAVIGATOR_FRAGMENT_TAG)
                if (existingFragment != null) {
                    // Use commitNow to ensure the fragment is removed synchronously
                    // before onSaveInstanceState is called
                    activity.supportFragmentManager.commitNow(allowStateLoss = true) {
                        remove(existingFragment)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Also clean up on dispose in case ON_STOP wasn't called
            navigatorController?.close()
            val existingFragment = activity.supportFragmentManager
                .findFragmentByTag(NAVIGATOR_FRAGMENT_TAG)
            if (existingFragment != null) {
                activity.supportFragmentManager.commit(allowStateLoss = true) {
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

/**
 * Creates decoration templates that respect the user's alpha choice from the color picker.
 *
 * Readium's default templates use `alpha = 0.3` which overrides any alpha value set in the
 * decoration's tint color. This function creates custom templates that use `toCss()` without
 * an alpha override, so the alpha from the color itself is used.
 */
private fun createUserAlphaDecorationTemplates(): HtmlDecorationTemplates {
    val defaultTint = android.graphics.Color.YELLOW
    val lineWeight = 2
    val cornerRadius = 3

    return HtmlDecorationTemplates {
        set(
            Decoration.Style.Highlight::class,
            createHighlightTemplate(defaultTint, lineWeight, cornerRadius)
        )
        set(
            Decoration.Style.Underline::class,
            createUnderlineTemplate(defaultTint, lineWeight, cornerRadius)
        )
    }
}

private var classNamesId = 0
private fun createUniqueClassName(key: String): String = "r2-$key-${++classNamesId}"

/**
 * Creates a highlight template that uses the color's own alpha value.
 */
private fun createHighlightTemplate(
    defaultTint: Int,
    lineWeight: Int,
    cornerRadius: Int,
): HtmlDecorationTemplate {
    val className = createUniqueClassName("highlight")

    return HtmlDecorationTemplate(
        layout = HtmlDecorationTemplate.Layout.BOXES,
        element = { decoration ->
            val tint = (decoration.style as? Decoration.Style.Tinted)?.tint ?: defaultTint
            val isActive = (decoration.style as? Decoration.Style.Activable)?.isActive ?: false
            val css = buildString {
                // Use toCss() without alpha parameter to respect the color's own alpha
                append("background-color: ${tint.toCss()} !important;")
                if (isActive) {
                    append("--underline-color: ${tint.toCss()};")
                }
            }
            """<div class="$className" style="$css"/>"""
        },
        stylesheet = createDecorationStylesheet(className, lineWeight, cornerRadius)
    )
}

/**
 * Creates an underline template that uses the color's own alpha value.
 */
private fun createUnderlineTemplate(
    defaultTint: Int,
    lineWeight: Int,
    cornerRadius: Int,
): HtmlDecorationTemplate {
    val className = createUniqueClassName("underline")

    return HtmlDecorationTemplate(
        layout = HtmlDecorationTemplate.Layout.BOXES,
        element = { decoration ->
            val tint = (decoration.style as? Decoration.Style.Tinted)?.tint ?: defaultTint
            val isActive = (decoration.style as? Decoration.Style.Activable)?.isActive ?: false
            val css = buildString {
                if (isActive) {
                    // Use toCss() without alpha parameter to respect the color's own alpha
                    append("background-color: ${tint.toCss()} !important;")
                }
                append("--underline-color: ${tint.toCss()};")
            }
            """<div class="$className" style="$css"/>"""
        },
        stylesheet = createDecorationStylesheet(className, lineWeight, cornerRadius)
    )
}

private fun createDecorationStylesheet(
    className: String,
    lineWeight: Int,
    cornerRadius: Int,
): String = """
    .$className {
        margin: 0px -1px 0 0;
        padding: 0 2px 0px 0;
        border-radius: ${cornerRadius}px;
        box-sizing: border-box;
        border: 0 solid var(--underline-color);
    }

    /* Horizontal (default) */
    [data-writing-mode="horizontal-tb"].$className {
        border-bottom-width: ${lineWeight}px;
    }

    /* Vertical right-to-left */
    [data-writing-mode="vertical-rl"].$className,
    [data-writing-mode="sideways-rl"].$className {
        border-left-width: ${lineWeight}px;
    }

    /* Vertical left-to-right */
    [data-writing-mode="vertical-lr"].$className,
    [data-writing-mode="sideways-lr"].$className {
        border-right-width: ${lineWeight}px;
    }
"""
