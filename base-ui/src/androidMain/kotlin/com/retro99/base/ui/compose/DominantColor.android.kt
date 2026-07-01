package com.retro99.base.ui.compose

import androidx.compose.ui.graphics.Color
import coil3.Image
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap

private const val SAMPLE_SIZE = 48

actual fun Image.dominantColor(): Color {
    val targetWidth = minOf(width, SAMPLE_SIZE).coerceAtLeast(1)
    val targetHeight = minOf(height, SAMPLE_SIZE).coerceAtLeast(1)
    val bitmap = toBitmap(targetWidth, targetHeight)
    val pixels = IntArray(targetWidth * targetHeight)
    bitmap.getPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)
    return averageColorFromARGBPixels(pixels)
}

actual fun ImageRequest.Builder.configureForPixelAccess(): ImageRequest.Builder =
    allowHardware(false)
