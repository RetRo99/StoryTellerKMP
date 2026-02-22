package com.retro99.reader.ui.publication

import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel

/**
 * Wrapper data class that holds an [EpubPublication] along with its current settings and position.
 *
 * This allows the settings and position to be updated via `.copy()` while keeping them
 * associated with the publication. This is important for handling configuration changes
 * (like screen rotation) where the reader view needs to be recreated with the current
 * settings and position, not the initial values.
 *
 * @property publication The opened EPUB publication (platform-specific wrapper)
 * @property settings The current reader settings (font size, theme, etc.)
 * @property position The current reading position, or null if at the beginning
 */
data class PublicationState(
    val publication: EpubPublication,
    val settings: ReaderSettingsUiModel,
    val position: PositionUiModel?,
)

