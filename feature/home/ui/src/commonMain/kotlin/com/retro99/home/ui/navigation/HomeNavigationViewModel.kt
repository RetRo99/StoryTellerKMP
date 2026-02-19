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
import com.retro99.user.api.UserRegistry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
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
    @Provided private val userRegistry: UserRegistry,
) : BaseViewModel<HomeUiState, HomeNavigationIntent>(
    HomeUiState(),
) {

    private val _navigationEvents = MutableSharedFlow<HomeNavigationEvent>(extraBufferCapacity = 1)

    /**
     * Navigation events that should be consumed by the composable to perform navigation.
     * These are one-shot events for deep links and "open last book on launch".
     */
    val navigationEvents: SharedFlow<HomeNavigationEvent> = _navigationEvents.asSharedFlow()

    private val _userProfileChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * Emits when the active user profile changes.
     * Used to reset navigation state when switching users.
     */
    val userProfileChanged: SharedFlow<Unit> = _userProfileChanged.asSharedFlow()

    init {
        observeDeepLinks(deepLinkHandler)
        observeUserProfileChanges()
        loadCurrentlyReading()
        loadBubblePosition()
        checkOpenLastBookOnLaunch()
    }

    /**
     * Observes user profile changes and emits events to reset navigation.
     * Uses drop(1) to skip the initial emission when the ViewModel is created.
     */
    private fun observeUserProfileChanges() {
        userRegistry.observeActiveProfile()
            .map { it?.id }
            .distinctUntilChanged()
            .drop(1) // Skip initial value to avoid resetting on first load
            .onEach {
                // Emit event to reset navigation
                _userProfileChanged.emit(Unit)
                // Refresh user-scoped data for the new user
                loadCurrentlyReading()
                loadBubblePosition()
            }
            .launchIn(viewModelScope)
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
                serverId = currentlyReading.serverId,
                bookUuid = currentlyReading.bookUuid,
                bookType = currentlyReading.bookType,
                tab = HomeTab.Books,
            )
        )
    }

    /**
     * Loads the bubble position from user-scoped preferences.
     */
    private fun loadBubblePosition() {
        val userId = userRegistry.getActiveProfileIdOrDefault()
        val key = PreferencesKey.UserScoped(userId, PreferencesKey.BubblePosition.name)
        val bubblePosition = preferences.getObject<BubblePositionModel>(key)
            ?: BubblePositionModel.DEFAULT
        updateState { it.copy(bubblePosition = bubblePosition) }
    }

    /**
     * Saves the bubble position to user-scoped preferences.
     */
    private fun saveBubblePosition(side: BubbleSide, yFraction: Float) {
        val userId = userRegistry.getActiveProfileIdOrDefault()
        val key = PreferencesKey.UserScoped(userId, PreferencesKey.BubblePosition.name)
        val position = BubblePositionModel.fromBubbleSide(side, yFraction)
        preferences.putObject(key, position)
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
                        serverId = destination.serverId,
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
                        HomeDestination.Reader(
                            serverId = intent.serverId,
                            bookUuid = intent.bookUuid,
                            bookType = intent.bookType,
                        )
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

