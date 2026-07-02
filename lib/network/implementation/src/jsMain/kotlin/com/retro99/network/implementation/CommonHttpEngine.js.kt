package com.retro99.network.implementation

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

actual fun getHttpEngine(): HttpClientEngineFactory<*> = Js
