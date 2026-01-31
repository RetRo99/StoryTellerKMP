package com.retro99.reader.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.retro99.reader.domain.model.ReaderSettingsDomainModel

/**
 * Platform-specific EPUB reader view.
 * On Android, this uses Readium's EpubNavigatorFragment.
 * On iOS, this uses Readium Swift via bridge.
 *
 * @param bookUuid The unique identifier of the book
 * @param initialSettings The initial reader settings to apply when opening the publication
 * @param modifier The modifier to apply to the view
 */
@Composable
internal expect fun EpubReaderView(
    bookUuid: String,
    initialSettings: ReaderSettingsDomainModel,
    modifier: Modifier = Modifier,
)

