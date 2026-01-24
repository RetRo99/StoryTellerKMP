package com.retro99.network.implementation

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun getHttpEngine(): HttpClientEngineFactory<*> = Darwin