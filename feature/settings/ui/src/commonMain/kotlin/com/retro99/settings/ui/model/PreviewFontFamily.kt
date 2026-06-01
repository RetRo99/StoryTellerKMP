package com.retro99.settings.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

@Composable
expect fun FontFamilyUiModel.toPreviewFontFamily(): FontFamily

@Composable
expect fun FontFamilyUiModel.toWeightedPreviewFontFamily(fontWeight: Double): FontFamily
