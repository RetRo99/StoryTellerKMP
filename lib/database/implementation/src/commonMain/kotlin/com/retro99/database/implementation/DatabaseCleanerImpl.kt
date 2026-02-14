package com.retro99.database.implementation

import com.retro99.database.api.DataClearable
import com.retro99.database.api.DatabaseCleaner
import org.koin.core.annotation.Single

@Single(binds = [DatabaseCleaner::class])
internal class DatabaseCleanerImpl(
    private val dataClearables: List<DataClearable>,
) : DatabaseCleaner {

    override suspend fun clearAllData() {
        dataClearables.forEach { it.clearAllData() }
    }
}

