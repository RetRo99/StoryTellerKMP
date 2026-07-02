package com.retro99.settings.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

@Composable
actual fun FontFamilyUiModel.toPreviewFontFamily(): FontFamily = fallbackPreviewFontFamily()

@Composable
actual fun FontFamilyUiModel.toWeightedPreviewFontFamily(fontWeight: Double): FontFamily =
    if (fontWeight == 1.0) {
        toPreviewFontFamily()
    } else {
        fallbackPreviewFontFamily()
    }

internal fun FontFamilyUiModel.fallbackPreviewFontFamily(): FontFamily = when (cssValue) {
    FontFamilyUiModel.SERIF.cssValue,
    "Literata",
    "Merriweather",
    "Source Serif 4",
    "Noto Serif",
    -> FontFamily.Serif

    FontFamilyUiModel.MONOSPACE.cssValue,
    FontFamilyUiModel.IA_WRITER_DUOSPACE.cssValue,
    -> FontFamily.Monospace

    FontFamilyUiModel.CURSIVE.cssValue -> FontFamily.Cursive

    else -> FontFamily.SansSerif
}
