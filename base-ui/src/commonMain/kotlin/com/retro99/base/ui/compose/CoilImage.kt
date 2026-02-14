package com.retro99.base.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.retro99.analytics.api.Analytics
import org.jetbrains.compose.resources.DrawableResource
import org.koin.compose.koinInject

@Composable
fun CoilImage(
    data: Any?,
    cacheKey: String?,
    modifier: Modifier = Modifier,
    placeholder: DrawableResource? = null,
    onState: ((AsyncImagePainter.State) -> Unit)? = null,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val analytics = koinInject<Analytics>()

    val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
        .data(data ?: placeholder)
        .crossfade(true)
        .apply {
            if (cacheKey != null) {
                memoryCacheKey(cacheKey)
                diskCacheKey(cacheKey)
            }
        }
        .build()

    AsyncImage(
        modifier = modifier,
        model = imageRequest,
        contentDescription = contentDescription,
        contentScale = contentScale,
        onState = { state ->
            onState?.invoke(state)
            if (state is AsyncImagePainter.State.Error) {
                val throwable = state.result.throwable
                // Don't log the URL/data for privacy - only log cache key if available
                analytics.logException(
                    throwable = throwable,
                    message = "Image Load Failed${cacheKey?.let { " for cacheKey=$it" } ?: ""}"
                )
            }
        },
    )
}