package com.retro99.network.implementation.di

import com.retro99.network.implementation.HttpClientProvider
import com.retro99.network.implementation.getHttpEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
@ComponentScan("com.retro99.network.implementation")
class NetworkingModule {

    @Single
    fun provideHttpClientEngineFactory(): HttpClientEngineFactory<*> = getHttpEngine()

    @Single
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Single
    fun provideHttpClient(
        provider: HttpClientProvider,
    ): HttpClient = provider.provide()
}
