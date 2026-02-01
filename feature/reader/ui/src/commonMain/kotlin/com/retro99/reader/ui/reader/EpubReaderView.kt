package com.retro99.reader.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.ui.publication.EpubPublication
import kotlinx.coroutines.flow.Flow

/**
 * Platform-specific EPUB reader view.
 * On Android, this uses Readium's EpubNavigatorFragment.
 * On iOS, this uses Readium Swift via bridge.
 *
 * @param bookUuid The unique identifier of the book
 * @param publication The opened EPUB publication
 * @param settings The reader settings to apply (reactive - updates when changed)
 * @param commands Flow of commands from ViewModel for navigation and settings
 * @param onProgressChanged Callback when the reading progress changes
 * @param modifier The modifier to apply to the view
 */
@Composable
internal expect fun EpubReaderView(
    bookUuid: String,
    publication: EpubPublication,
    settings: ReaderSettingsDomainModel,
    commands: Flow<ReaderCommand>,
    onProgressChanged: (locator: String, progression: Float) -> Unit,
    modifier: Modifier = Modifier,
)

