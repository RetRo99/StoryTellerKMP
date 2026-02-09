package com.retro99.home.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigation3.ui.NavDisplay
import com.retro99.home.ui.navigation.BottomSheetSceneStrategy.Companion.bottomSheet

/**
 * Interface for destinations that can be displayed as a bottom sheet.
 * Implement this interface and override [isBottomSheet] to return `true`
 * for destinations that should be displayed as bottom sheets.
 */
interface BottomSheetDestination {
    val isBottomSheet: Boolean
        get() = false
}

/**
 * An [OverlayScene] that renders an [entry] within a [ModalBottomSheet].
 */
@OptIn(ExperimentalMaterial3Api::class)
internal class BottomSheetScene<T : Any>(
    override val key: T,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable (() -> Unit) = {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onBack,
            sheetState = sheetState,
        ) {
            entry.Content()
        }
    }
}

/**
 * A [SceneStrategy] that displays entries that have added [bottomSheet] to their [NavEntry.metadata]
 * within a [ModalBottomSheet] instance.
 *
 * This strategy should always be added before any non-overlay scene strategies.
 *
 * To mark a destination as a bottom sheet:
 * 1. Implement [BottomSheetDestination] and override [BottomSheetDestination.isBottomSheet] to return `true`.
 * 2. Wrap your entry provider with [bottomSheetEntryProvider] to automatically add metadata.
 *
 * Example:
 * ```
 * NavDisplay(
 *     sceneStrategy = BottomSheetSceneStrategy(),
 *     entryProvider = bottomSheetEntryProvider(
 *         entryProvider {
 *             entry<MyDestination.Settings> { SettingsScreen() }
 *         }
 *     )
 * )
 * ```
 */
class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull() ?: return null
        val isBottomSheet = lastEntry.metadata[BOTTOM_SHEET_KEY] as? Boolean ?: false

        return if (isBottomSheet) {
            @Suppress("UNCHECKED_CAST")
            BottomSheetScene(
                key = lastEntry.contentKey as T,
                previousEntries = entries.dropLast(1),
                overlaidEntries = entries.dropLast(1),
                entry = lastEntry,
                onBack = onBack,
            )
        } else {
            null
        }
    }

    companion object {
        internal const val BOTTOM_SHEET_KEY = "bottomSheet"

        /**
         * Creates metadata to mark an entry as a bottom sheet destination.
         *
         * Add this to the `metadata` parameter of the `entry` function to display
         * the destination as a modal bottom sheet.
         */
        fun bottomSheet(): Map<String, Any> = mapOf(BOTTOM_SHEET_KEY to true)
    }
}

/**
 * Wraps an entry provider function to automatically add bottom sheet metadata.
 *
 * This function takes a base entry provider and wraps it to automatically check if the key
 * implements [BottomSheetDestination] with [BottomSheetDestination.isBottomSheet] = true.
 * If so, it creates a new [NavEntry] with the bottom sheet metadata added.
 *
 * Usage:
 * ```
 * NavDisplay(
 *     entryProvider = bottomSheetEntryProvider(
 *         entryProvider {
 *             entry<HomeDestination.BooksList> { BooksListScreen() }
 *             entry<HomeDestination.Settings> { SettingsScreen() }
 *         }
 *     )
 * )
 * ```
 *
 * @param baseProvider The base entry provider function that creates NavEntry objects.
 * @return A wrapped entry provider that automatically adds bottom sheet metadata.
 */
fun <T : Any> bottomSheetEntryProvider(
    baseProvider: (key: T) -> NavEntry<T>,
): (key: T) -> NavEntry<T> = { key: T ->
    val baseEntry = baseProvider(key)
    val shouldBeBottomSheet = (key as? BottomSheetDestination)?.isBottomSheet == true

    if (shouldBeBottomSheet && baseEntry.metadata[BottomSheetSceneStrategy.BOTTOM_SHEET_KEY] != true) {
        NavEntry(
            key = key,
            metadata = baseEntry.metadata + bottomSheet(),
            content = { baseEntry.Content() },
        )
    } else {
        baseEntry
    }
}

/**
 * A custom [NavDisplay] wrapper that automatically handles bottom sheet destinations.
 *
 * This composable wraps the standard [NavDisplay] and automatically:
 * - Applies [BottomSheetSceneStrategy] as the scene strategy
 * - Wraps the entry provider with [bottomSheetEntryProvider] to add bottom sheet metadata
 * - Provides default entry decorators for state saving and ViewModel management
 *
 * Any destination that implements [BottomSheetDestination] with [BottomSheetDestination.isBottomSheet] = true
 * will automatically be displayed as a modal bottom sheet.
 *
 * Usage:
 * ```
 * BottomSheetNavDisplay(
 *     backStack = state.backStack,
 *     onBack = { handleBack() },
 *     entryProvider = entryProvider {
 *         entry<MyDestination.Home> { HomeScreen() }
 *         entry<MyDestination.Settings> { SettingsScreen() } // Opens as bottom sheet
 *     }
 * )
 * ```
 *
 * @param backStack The list of navigation entries representing the current navigation state.
 * @param onBack Callback invoked when the user navigates back.
 * @param entryProvider Function that provides [NavEntry] for each navigation key.
 * @param modifier Modifier to be applied to the NavDisplay.
 * @param entryDecorators List of decorators to apply to navigation entries. Defaults to state
 *   saving and ViewModel management decorators.
 */
@Composable
fun <T : Any> BottomSheetNavDisplay(
    backStack: List<T>,
    onBack: () -> Unit,
    entryProvider: (key: T) -> NavEntry<T>,
    modifier: Modifier = Modifier,
    entryDecorators: List<NavEntryDecorator<T>> = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
    ),
) {
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<T>() }

    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        modifier = modifier,
        sceneStrategy = bottomSheetStrategy,
        entryDecorators = entryDecorators,
        entryProvider = bottomSheetEntryProvider(entryProvider),
    )
}
