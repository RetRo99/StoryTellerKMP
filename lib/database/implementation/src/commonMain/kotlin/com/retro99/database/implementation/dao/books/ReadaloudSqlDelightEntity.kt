package com.retro99.database.implementation.dao.books

import com.retro99.database.api.books.ReadaloudEntity

/**
 * SQLDelight entity for readalouds table.
 */
data class ReadaloudSqlDelightEntity(
    override val uuid: String,
    override val bookUuid: String,
    override val filepath: String,
    override val missing: Int?,
    override val status: String?,
    override val currentStage: String?,
    override val stageProgress: Int?,
    override val queuePosition: Int?,
    override val restartPending: Boolean?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : ReadaloudEntity

