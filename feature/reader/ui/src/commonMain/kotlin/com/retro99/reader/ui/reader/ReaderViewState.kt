package com.retro99.reader.ui.reader

import com.retro99.base.result.AppError
import com.retro99.books.ui.model.LocatorUiModel
import com.retro99.reader.ui.publication.EpubPublication

data class ReaderViewState(
    val bookUuid: String? = null,
    val localFilePath: String? = null,
    val publication: EpubPublication? = null,
    val locator: LocatorUiModel? = null,
    val isSettingsVisible: Boolean = false,
    val error: AppError? = null,
)

