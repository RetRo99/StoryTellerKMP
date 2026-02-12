package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.books.data.model.SeriesApiModel

interface SeriesRemoteSource {

    suspend fun getSeries(): AppResult<List<SeriesApiModel>>
}

