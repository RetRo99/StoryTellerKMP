package com.retro99.network.implementation

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig

internal expect fun HttpClientConfig<out HttpClientEngineConfig>.installCorsProxyIfNeeded()
