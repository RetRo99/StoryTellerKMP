package com.retro99.base.ui.compose

import androidx.compose.ui.graphics.Color
import coil3.Image
import coil3.request.ImageRequest
import coil3.toBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

private const val SAMPLE_SIZE = 48

actual fun Image.dominantColor(): Color {
    val targetWidth = minOf(width, SAMPLE_SIZE).coerceAtLeast(1)
    val targetHeight = minOf(height, SAMPLE_SIZE).coerceAtLeast(1)
    val bitmap: Bitmap = toBitmap(targetWidth, targetHeight)
    val info = ImageInfo(
        ColorInfo(ColorType.RGBA_8888, ColorAlphaType.PREMUL, null),
        targetWidth,
        targetHeight,
    )
    val rowBytes = targetWidth * 4
    val bytes = bitmap.readPixels(info, rowBytes, 0, 0) ?: return Color.Black
    return averageColorFromRGBAPremulBytes(bytes)
}

actual fun ImageRequest.Builder.configureForPixelAccess(): ImageRequest.Builder = this
