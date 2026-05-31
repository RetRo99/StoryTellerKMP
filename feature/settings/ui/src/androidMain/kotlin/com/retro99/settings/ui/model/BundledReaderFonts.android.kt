package com.retro99.settings.ui.model

internal actual fun platformBundledReaderFonts(): List<FontFamilyUiModel> =
    listOf(
        FontFamilyUiModel(
            id = "droid_sans",
            cssValue = "Droid Sans",
            displayName = "Droid Sans",
        ),
        FontFamilyUiModel(
            id = "atkinson_hyperlegible",
            cssValue = "Atkinson Hyperlegible",
            displayName = "Atkinson Hyperlegible",
        ),
        FontFamilyUiModel(
            id = "literata",
            cssValue = "Literata",
            displayName = "Literata",
        ),
        FontFamilyUiModel(
            id = "merriweather",
            cssValue = "Merriweather",
            displayName = "Merriweather",
        ),
        FontFamilyUiModel(
            id = "source_serif_4",
            cssValue = "Source Serif 4",
            displayName = "Source Serif 4",
        ),
        FontFamilyUiModel(
            id = "noto_sans",
            cssValue = "Noto Sans",
            displayName = "Noto Sans",
        ),
        FontFamilyUiModel(
            id = "noto_serif",
            cssValue = "Noto Serif",
            displayName = "Noto Serif",
        ),
    )
