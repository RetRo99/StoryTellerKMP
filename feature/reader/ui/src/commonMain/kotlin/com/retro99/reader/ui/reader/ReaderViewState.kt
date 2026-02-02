package com.retro99.reader.ui.reader

import com.retro99.base.result.AppError
import com.retro99.reader.ui.model.ReadingProgressUiModel
import com.retro99.reader.ui.publication.EpubPublication

data class ReaderViewState(
    val bookUuid: String? = null,
    val localFilePath: String? = null,
    val publication: EpubPublication? = null,
    val progress: ReadingProgressUiModel? = null,
    val isSettingsVisible: Boolean = false,
    val error: AppError? = null,
)

