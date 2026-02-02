package com.retro99.books.data.model

import com.retro99.books.domain.model.ReadaloudDomainModel
import com.retro99.database.api.books.ReadaloudEntity

data class ReadaloudLocalModel(
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

fun ReadaloudLocalModel.toDomain(): ReadaloudDomainModel {
    return ReadaloudDomainModel(
        uuid = uuid,
        filepath = filepath,
        missing = missing,
        status = status,
        currentStage = currentStage,
        stageProgress = stageProgress,
        queuePosition = queuePosition,
        restartPending = restartPending,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun ReadaloudDomainModel.toLocal(bookUuid: String): ReadaloudLocalModel {
    return ReadaloudLocalModel(
        uuid = uuid,
        bookUuid = bookUuid,
        filepath = filepath,
        missing = missing,
        status = status,
        currentStage = currentStage,
        stageProgress = stageProgress,
        queuePosition = queuePosition,
        restartPending = restartPending,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

