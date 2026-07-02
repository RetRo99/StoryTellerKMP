package com.retro99.network.implementation

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig

internal actual fun HttpClientConfig<out HttpClientEngineConfig>.installCorsProxyIfNeeded() {
}
