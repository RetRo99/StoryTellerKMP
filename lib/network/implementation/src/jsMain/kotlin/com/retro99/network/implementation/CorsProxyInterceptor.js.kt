package com.retro99.network.implementation

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.takeFrom

private val CorsProxyPlugin = createClientPlugin("CorsProxyPlugin") {
    onRequest { request, _ ->
        val original = request.url
        val urlString = original.toString()
        if (urlString.startsWith("http") && !urlString.contains("localhost")) {
            val pathWithQuery = urlString.substringAfter("://").substringAfter("/")
            val proxiedUrl = "http://localhost:8080/proxy/$pathWithQuery"
            request.url.takeFrom(proxiedUrl)
        }
    }
}

internal actual fun HttpClientConfig<out HttpClientEngineConfig>.installCorsProxyIfNeeded() {
    install(CorsProxyPlugin)
}
