package com.retro99.reader.ui.di

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import org.koin.core.qualifier.Qualifier
import org.koin.mp.KoinPlatform.getKoin

/**
 * Composable function to inject dependencies from the [ReaderScope].
 *
 * This function retrieves or creates a Koin scope identified by the [bookUuid]
 * and injects the requested dependency from that scope.
 *
 * @param T The type of dependency to inject
 * @param bookUuid The unique identifier of the book, used as the scope ID
 * @param qualifier Optional Koin qualifier for the dependency
 * @return The injected dependency of type [T]
 */
@Composable
inline fun <reified T : Any> koinReaderScopeInject(
    bookUuid: String,
    qualifier: Qualifier? = null,
): T {
    val scope = getKoin().getOrCreateScope<ReaderScope>(bookUuid)

    return koinInject(
        scope = scope,
        qualifier = qualifier,
    )
}

