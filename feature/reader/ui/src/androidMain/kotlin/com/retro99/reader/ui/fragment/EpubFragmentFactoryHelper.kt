package com.retro99.reader.ui.fragment

import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import org.readium.r2.navigator.epub.EpubNavigatorFragment

/**
 * Helper object for managing EpubNavigatorFragment factory.
 *
 * This is needed because EpubNavigatorFragment requires factory instantiation
 * (no default constructor). When Android tries to restore the fragment after
 * process death, it fails with Fragment$InstantiationException.
 *
 * The solution is to:
 * 1. Set a dummy fragment factory BEFORE super.onCreate() in the Activity
 * 2. Remove any restored fragment AFTER super.onCreate() but BEFORE onResume()
 *
 * This is the official Readium approach used in their test app.
 *
 * Usage in MainActivity:
 * ```kotlin
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     supportFragmentManager.fragmentFactory = EpubFragmentFactoryHelper.createDummyFactory()
 *     super.onCreate(savedInstanceState)
 *     EpubFragmentFactoryHelper.removeRestoredFragment(supportFragmentManager)
 *     // ...
 * }
 * ```
 */
object EpubFragmentFactoryHelper {

    // Must match the prefix used in EpubReaderView.android.kt
    private const val NAVIGATOR_FRAGMENT_TAG_PREFIX = "epub_navigator_"

    /**
     * Creates a dummy fragment factory that prevents crashes when Android
     * tries to restore EpubNavigatorFragment after process death.
     *
     * The dummy factory creates a placeholder fragment that will be replaced
     * when the reader screen properly initializes the navigator.
     */
    fun createDummyFactory(): FragmentFactory {
        return EpubNavigatorFragment.createDummyFactory()
    }

    /**
     * Removes any restored EpubNavigatorFragment from the FragmentManager.
     *
     * This must be called AFTER super.onCreate() but BEFORE onResume() to prevent
     * RestorationNotSupportedException from being thrown when the dummy fragment
     * tries to resume.
     *
     * @param fragmentManager The activity's supportFragmentManager
     */
    fun removeRestoredFragment(fragmentManager: FragmentManager) {
        val restoredFragments = fragmentManager.fragments.filter { fragment ->
            fragment is EpubNavigatorFragment ||
                    fragment.tag?.startsWith(NAVIGATOR_FRAGMENT_TAG_PREFIX) == true
        }
        if (restoredFragments.isNotEmpty()) {
            fragmentManager.commitNow(allowStateLoss = true) {
                restoredFragments.forEach { remove(it) }
            }
        }
    }
}

