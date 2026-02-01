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
 * @param localFilePath The local file path of the EPUB file
 * @param settings The reader settings to apply (reactive - updates when changed)
 * @param onReady Callback when the publication is successfully opened
 * @param onProgressChanged Callback when the reading progress changes
 * @param modifier The modifier to apply to the view
 */
@Composable
internal expect fun EpubReaderView(
    bookUuid: String,
    localFilePath: String,
    settings: ReaderSettingsDomainModel,
    onProgressChanged: (locator: String, progression: Float) -> Unit,
    modifier: Modifier = Modifier,
)

