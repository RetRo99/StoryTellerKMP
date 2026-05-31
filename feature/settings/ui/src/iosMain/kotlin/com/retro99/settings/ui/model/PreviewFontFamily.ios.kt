package com.retro99.settings.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

@Composable
actual fun FontFamilyUiModel.toPreviewFontFamily(): FontFamily = when (cssValue) {
    FontFamilyUiModel.SERIF.cssValue -> FontFamily.Serif
    FontFamilyUiModel.MONOSPACE.cssValue,
    FontFamilyUiModel.IA_WRITER_DUOSPACE.cssValue,
    -> FontFamily.Monospace

    FontFamilyUiModel.CURSIVE.cssValue -> FontFamily.Cursive
    else -> FontFamily.SansSerif
}
