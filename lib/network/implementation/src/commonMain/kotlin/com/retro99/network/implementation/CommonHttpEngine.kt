package com.retro99.network.implementation

import io.ktor.client.engine.HttpClientEngineFactory

expect fun getHttpEngine(): HttpClientEngineFactory<*>