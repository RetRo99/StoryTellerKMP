package com.retro99.parrot.initializer

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.retro99.base.AppInitializer
import io.ktor.client.HttpClient
import org.koin.core.annotation.Single

@Single(binds = [AppInitializer::class])
class CoilInitializer(
    private val httpClient: HttpClient,
) : AppInitializer {

    override fun initialize() {
        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components {
                    add(KtorNetworkFetcherFactory(httpClient))
                }
                .build()
        }
    }
}

