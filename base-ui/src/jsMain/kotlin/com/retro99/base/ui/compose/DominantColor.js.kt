package com.retro99.base.ui.compose

import androidx.compose.ui.graphics.Color
import coil3.Image
import coil3.request.ImageRequest

actual fun Image.dominantColor(): Color = Color.Gray

actual fun ImageRequest.Builder.configureForPixelAccess(): ImageRequest.Builder = this
