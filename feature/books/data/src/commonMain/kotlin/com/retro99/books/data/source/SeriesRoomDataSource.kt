package com.retro99.books.data.source

import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.books.data.model.SeriesLocalModel
import com.retro99.books.data.model.toLocalModel
import com.retro99.database.api.DatabaseExecutor
import com.retro99.database.api.books.BooksDatabase
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [SeriesLocalSource::class])
internal class SeriesRoomDataSource(
    @Provided private val booksDatabase: BooksDatabase,
    @Provided private val databaseExecutor: DatabaseExecutor,
) : SeriesLocalSource {

    override suspend fun getSeries(): AppResult<List<SeriesLocalModel>?> {
        return databaseExecutor.executeDatabaseOperation {
            val series = booksDatabase.getAllSeries()
            if (series.isEmpty()) {
                null
            } else {
                series.map { it.toLocalModel() }
            }
        }
    }

    override suspend fun saveSeries(series: List<SeriesLocalModel>): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            series.forEach { seriesItem ->
                booksDatabase.upsertSeries(seriesItem)
            }
        }
    }

    override suspend fun clearCache(): CompletableResult {
        return databaseExecutor.executeDatabaseOperation {
            booksDatabase.deleteAllSeries()
        }
    }
}

