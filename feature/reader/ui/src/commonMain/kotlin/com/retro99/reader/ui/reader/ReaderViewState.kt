package com.retro99.reader.ui.reader

import com.retro99.base.result.AppError
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.domain.model.ReadingProgressDomainModel

data class ReaderViewState(
    val bookUuid: String? = null,
    val localFilePath: String? = null,
    val isPublicationReady: Boolean = false,
    val settings: ReaderSettingsDomainModel = ReaderSettingsDomainModel(),
    val progress: ReadingProgressDomainModel? = null,
    val isSettingsVisible: Boolean = false,
    val error: AppError? = null,
)

