package com.retro99.books.domain

import com.retro99.base.result.AppResult
import com.retro99.books.domain.model.SeriesDomainModel
import kotlinx.coroutines.flow.Flow

interface SeriesRepository {

    fun getSeries(): Flow<AppResult<List<SeriesDomainModel>>>
}

