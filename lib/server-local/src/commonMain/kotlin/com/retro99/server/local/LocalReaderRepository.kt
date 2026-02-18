package com.retro99.server.local

import com.github.michaelbull.result.Ok
import com.retro99.base.result.AppResult
import com.retro99.base.result.CompletableResult
import com.retro99.server.api.ServerPosition
import com.retro99.server.api.ServerReaderRepository

/**
 * Local implementation of ServerReaderRepository.
 * Local books don't have remote position sync - positions are only stored locally.
 * This repository returns null/success for all operations since there's no remote server.
 */
class LocalReaderRepository(
    override val serverId: String,
) : ServerReaderRepository {

    override suspend fun getPosition(bookUuid: String): AppResult<ServerPosition?> {
        // Local books don't have remote position sync
        return Ok(null)
    }

    override suspend fun updatePosition(bookUuid: String, position: ServerPosition): CompletableResult {
        // Local books don't have remote position sync - just return success
        // The local cache is handled by ReaderDataRepository
        return Ok(Unit)
    }
}

