package com.retro99.reader.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.retro99.base.ui.IntentDispatcher
import com.retro99.reader.ui.di.koinReaderScopeInject
import com.retro99.reader.ui.navigator.BookController
import com.retro99.reader.ui.publication.EpubPublication

/**
 * Platform-specific EPUB reader view.
 * On Android, this uses Readium's EpubNavigatorFragment.
 * On iOS, this uses Readium Swift via bridge.
 *
 * @param bookUuid The unique identifier of the book
 * @param publication The opened EPUB publication
 * @param intentDispatcher Dispatcher for sending intents to the ViewModel
 * @param modifier The modifier to apply to the view
 * @param bookController Controller for navigating and controlling the book reader.
 *                       Defaults to injecting from the ReaderScope.
 */
@Composable
internal fun EpubReaderView(
    bookUuid: String,
    publication: EpubPublication,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    modifier: Modifier = Modifier,
    bookController: BookController = koinReaderScopeInject(bookUuid),
) {
    EpubReaderViewInternal(
        bookUuid = bookUuid,
        publication = publication,
        intentDispatcher = intentDispatcher,
        bookController = bookController,
        modifier = modifier,
    )
}

/**
 * Internal platform-specific EPUB reader view implementation.
 * On Android, this uses Readium's EpubNavigatorFragment.
 * On iOS, this uses Readium Swift via bridge.
 */
@Composable
internal expect fun EpubReaderViewInternal(
    bookUuid: String,
    publication: EpubPublication,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    bookController: BookController,
    modifier: Modifier = Modifier,
)

/**
 * Common composable that observes permission denied dialog state from the navigator.
 * Shows a dialog when notification permission is denied on Android.
 *
 * @param navigator The navigator controller to observe
 * @param showDialog Whether to show the permission denied dialog
 * @param showRationale Whether to show rationale (can ask again) vs settings (permanently denied)
 * @param onOpenSettings Callback to open app settings
 * @param onTryAgain Callback when user wants to try requesting permission again
 * @param onDismiss Callback when the dialog is dismissed
 */
@Composable
internal fun ObservePermissionDeniedDialog(
    navigator: BookController?,
    showDialog: Boolean,
    showRationale: Boolean = false,
    onOpenSettings: () -> Unit,
    onTryAgain: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    if (showDialog) {
        NotificationPermissionDeniedDialog(
            showRationale = showRationale,
            onOpenSettings = {
                onOpenSettings()
//                navigator?.dismissPermissionDeniedDialog()
            },
            onTryAgain = {
                onTryAgain()
//                navigator?.dismissPermissionDeniedDialog()
            },
            onDismiss = {
                onDismiss()
//                navigator?.dismissPermissionDeniedDialog()
            },
        )
    }
}
