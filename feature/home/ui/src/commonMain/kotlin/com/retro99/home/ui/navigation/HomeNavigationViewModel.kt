package com.retro99.home.ui.navigation

import androidx.lifecycle.viewModelScope
import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.NavigationAnalyticsEvent
import com.retro99.base.ui.BaseViewModel
import com.retro99.home.ui.deeplink.DeepLinkDestination
import com.retro99.home.ui.deeplink.DeepLinkHandler
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import com.retro99.preferences.api.getObject
import com.retro99.preferences.api.putObject
import com.retro99.reader.domain.usecase.GetCurrentlyReadingUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

/**
 * ViewModel for Home screen that handles non-navigation UI concerns.
 *
 * Navigation state is now managed by [HomeNavigationStateHolder] in the composable layer
 * using Nav3's [rememberNavBackStack] for automatic persistence across process death.
 *
 * This ViewModel handles:
 * - Currently reading book state
 * - Bubble position persistence
 * - Deep link navigation requests (emitted as [HomeNavigationEvent])
 * - "Open last book on launch" feature (emitted as [HomeNavigationEvent])
 * - Analytics for tab switches
 */
@KoinViewModel
class HomeNavigationViewModel(
    deepLinkHandler: DeepLinkHandler,
    @Provided val analytics: Analytics,
    @Provided private val getCurrentlyReadingUseCase: GetCurrentlyReadingUseCase,
    @Provided private val preferences: Preferences,
) : BaseViewModel<HomeUiState, HomeNavigationIntent>(
    HomeUiState(),
) {

    private val _navigationEvents = MutableSharedFlow<HomeNavigationEvent>(extraBufferCapacity = 1)

    /**
     * Navigation events that should be consumed by the composable to perform navigation.
     * These are one-shot events for deep links and "open last book on launch".
     */
    val navigationEvents: SharedFlow<HomeNavigationEvent> = _navigationEvents.asSharedFlow()

    init {
        observeDeepLinks(deepLinkHandler)
        loadCurrentlyReading()
        loadBubblePosition()
        checkOpenLastBookOnLaunch()
    }

    /**
     * Loads the currently reading book from preferences.
     * This is called on init and when returning from the reader.
     */
    private fun loadCurrentlyReading() {
        val currentlyReading = getCurrentlyReadingUseCase()?.toUiModel()
        updateState { it.copy(currentlyReading = currentlyReading) }
    }

    /**
     * Checks if the "Open Last Book on Launch" setting is enabled and
     * emits a navigation event to open the reader if there's a currently reading book.
     */
    private fun checkOpenLastBookOnLaunch() {
        val isEnabled = preferences.getBoolean(
            PreferencesKey.OpenLastBookOnLaunch,
            defaultValue = false,
        )
        if (!isEnabled) return

        val currentlyReading = getCurrentlyReadingUseCase() ?: return

        // Emit navigation event to open the reader
        emitNavigationEvent(
            HomeNavigationEvent.NavigateToReaderReplacing(
                bookUuid = currentlyReading.bookUuid,
                bookType = currentlyReading.bookType,
                tab = HomeTab.Books,
            )
        )
    }

    /**
     * Loads the bubble position from preferences.
     */
    private fun loadBubblePosition() {
        val bubblePosition = preferences.getObject<BubblePositionModel>(PreferencesKey.BubblePosition)
            ?: BubblePositionModel.DEFAULT
        updateState { it.copy(bubblePosition = bubblePosition) }
    }

    /**
     * Saves the bubble position to preferences.
     */
    private fun saveBubblePosition(side: BubbleSide, yFraction: Float) {
        val position = BubblePositionModel.fromBubbleSide(side, yFraction)
        preferences.putObject(PreferencesKey.BubblePosition, position)
        updateState { it.copy(bubblePosition = position) }
    }

    private fun observeDeepLinks(deepLinkHandler: DeepLinkHandler) {
        deepLinkHandler.navigationEvents
            .onEach { destination ->
                handleDeepLinkDestination(destination)
            }
            .launchIn(viewModelScope)
    }

    private fun handleDeepLinkDestination(destination: DeepLinkDestination) {
        when (destination) {
            is DeepLinkDestination.Reader -> {
                // Emit navigation event to open the reader
                emitNavigationEvent(
                    HomeNavigationEvent.NavigateToReaderReplacing(
                        bookUuid = destination.bookUuid,
                        bookType = destination.bookType,
                        tab = HomeTab.Books,
                    )
                )
            }
        }
    }

    override fun onIntent(intent: HomeNavigationIntent) {
        when (intent) {
            // UI state intents
            is HomeNavigationIntent.UpdateBubblePosition -> saveBubblePosition(intent.side, intent.yFraction)
            HomeNavigationIntent.RefreshCurrentlyReading -> loadCurrentlyReading()

            // Navigation intents - emit corresponding events
            is HomeNavigationIntent.NavigateTo -> {
                emitNavigationEvent(HomeNavigationEvent.NavigateTo(intent.destination))
            }
            is HomeNavigationIntent.SwitchTab -> {
                analytics.logEvent(NavigationAnalyticsEvent.TabSwitched(tabName = intent.tab.name.lowercase()))
                emitNavigationEvent(HomeNavigationEvent.SwitchTab(intent.tab))
            }
            HomeNavigationIntent.GoBack -> {
                emitNavigationEvent(HomeNavigationEvent.GoBack)
            }
            is HomeNavigationIntent.OpenReader -> {
                emitNavigationEvent(
                    HomeNavigationEvent.NavigateTo(
                        HomeDestination.Reader(intent.bookUuid, intent.bookType)
                    )
                )
            }
        }
    }

    private fun emitNavigationEvent(event: HomeNavigationEvent) {
        viewModelScope.launch {
            _navigationEvents.emit(event)
        }
    }
}

