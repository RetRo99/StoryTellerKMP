package com.retro99.network.implementation

import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import retro99.network.api.BaseUrlProvider
import kotlin.concurrent.Volatile

@Single(binds = [BaseUrlProvider::class])
class CachedBaseUrlProvider(
    @Provided private val preferences: Preferences,
) : BaseUrlProvider {

    @Volatile
    private var cachedUrl: String? = null

    override fun getBaseUrl(): String? {
        return cachedUrl ?: preferences.getStringOrNull(PreferencesKey.ServerUrl)?.also {
            cachedUrl = it
        }
    }

    override fun setBaseUrl(url: String) {
        cachedUrl = url
    }

    override fun clearBaseUrl() {
        cachedUrl = null
    }
}

