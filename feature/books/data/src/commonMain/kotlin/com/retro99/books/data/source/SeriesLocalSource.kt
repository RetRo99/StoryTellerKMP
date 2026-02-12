package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.books.data.model.SeriesLocalModel

interface SeriesLocalSource {

    suspend fun getSeries(): AppResult<List<SeriesLocalModel>?>

    suspend fun saveSeries(series: List<SeriesLocalModel>): CompletableResult

    suspend fun clearCache(): CompletableResult
}

