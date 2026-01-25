package com.retro99.base.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.jetbrains.compose.resources.DrawableResource

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
        onState = onState,
    )
}