package com.retro99.settings.ui.model

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily

@Composable
actual fun FontFamilyUiModel.toPreviewFontFamily(): FontFamily {
    val context = LocalContext.current
    previewFilePath?.let { filePath ->
        return remember(filePath) {
            runCatching {
                FontFamily(Typeface.createFromFile(filePath))
            }.getOrElse {
                fallbackPreviewFontFamily()
            }
        }
    }
    val assetPath = bundledPreviewFontAssets[cssValue]
    if (assetPath != null) {
        return remember(cssValue) {
            runCatching {
                FontFamily(Typeface.createFromAsset(context.assets, assetPath))
            }.getOrElse {
                fallbackPreviewFontFamily()
            }
        }
    }
    return fallbackPreviewFontFamily()
}

@Composable
actual fun FontFamilyUiModel.toWeightedPreviewFontFamily(fontWeight: Double): FontFamily =
    if (fontWeight == 1.0 || previewFilePath != null) {
        toPreviewFontFamily()
    } else {
        fallbackPreviewFontFamily()
    }

private fun FontFamilyUiModel.fallbackPreviewFontFamily(): FontFamily = when (cssValue) {
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

private val bundledPreviewFontAssets = mapOf(
    "Droid Sans" to "reader-fonts/bundled/DroidSans.ttf",
    "Atkinson Hyperlegible" to "reader-fonts/bundled/AtkinsonHyperlegible-Regular.ttf",
    "Literata" to "reader-fonts/bundled/Literata.ttf",
    "Merriweather" to "reader-fonts/bundled/Merriweather.ttf",
    "Source Serif 4" to "reader-fonts/bundled/SourceSerif4.ttf",
    "Noto Sans" to "reader-fonts/bundled/NotoSans.ttf",
    "Noto Serif" to "reader-fonts/bundled/NotoSerif.ttf",
)
