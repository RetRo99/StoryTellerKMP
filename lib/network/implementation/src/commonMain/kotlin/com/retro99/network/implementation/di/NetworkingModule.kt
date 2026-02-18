package com.retro99.network.implementation.di

import com.retro99.analytics.api.Analytics
import com.retro99.network.implementation.HttpClientProvider
import com.retro99.network.implementation.KtorNetworkClient
import com.retro99.network.implementation.getHttpEngine
import com.retro99.server.api.ServerRegistry
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import retro99.network.api.NetworkClient

@Module
@Configuration
@ComponentScan("com.retro99.network.implementation")
class NetworkingModule {

    @Single
    fun provideHttpClientEngineFactory(): HttpClientEngineFactory<*> = getHttpEngine()

    @Single
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Single
    fun provideHttpClient(
        provider: HttpClientProvider,
    ): HttpClient = provider.provide()

    @Single
    fun provideNetworkClient(
        httpClient: HttpClient,
        serverRegistry: ServerRegistry,
        analytics: Analytics,
    ): NetworkClient = KtorNetworkClient(
        httpClient = httpClient,
        baseUrlProvider = {
            // Use runBlocking since this is called from non-suspend context
            // This is safe because getActiveServer is a quick in-memory lookup
            runBlocking { serverRegistry.getActiveServer()?.baseUrl }
        },
        analytics = analytics,
    )
}
