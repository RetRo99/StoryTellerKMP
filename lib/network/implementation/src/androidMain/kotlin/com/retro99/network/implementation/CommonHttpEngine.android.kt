package com.retro99.network.implementation

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual fun getHttpEngine(): HttpClientEngineFactory<*> = OkHttp