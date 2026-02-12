package com.retro99.home.ui.navigation

/**
 * State for the Home navigation with multiple back stacks.
 *
 * Each tab maintains its own back stack, allowing users to navigate within a tab
 * and switch between tabs without losing their navigation state.
 *
 * @property currentTab The currently selected tab in the bottom navigation.
 * @property backStacks A map of back stacks, one for each tab. Each back stack
 *   starts with the tab's start destination.
 */
data class HomeNavigationState(
    val currentTab: HomeTab = HomeTab.DEFAULT,
    val backStacks: Map<HomeTab, List<HomeDestination>> = HomeTab.entries.associateWith {
        listOf(it.startDestination)
    },
) {
    /**
     * Returns the current back stack for the selected tab.
     */
    val currentBackStack: List<HomeDestination>
        get() = backStacks[currentTab] ?: listOf(currentTab.startDestination)

    /**
     * Returns the combined back stack for NavDisplay.
     *
     * This follows the "exit through home" pattern where the start tab's entries
     * are always at the bottom of the stack, ensuring the user exits through
     * the starting tab.
     */
    val combinedBackStack: List<HomeDestination>
        get() = if (currentTab == HomeTab.DEFAULT) {
            currentBackStack
        } else {
            // Include the start tab's entries at the bottom, then current tab's entries
            val startTabStack =
                backStacks[HomeTab.DEFAULT] ?: listOf(HomeTab.DEFAULT.startDestination)
            startTabStack + currentBackStack
        }
}

