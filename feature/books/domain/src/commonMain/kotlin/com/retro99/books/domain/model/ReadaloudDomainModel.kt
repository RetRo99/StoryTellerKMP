package com.retro99.books.domain.model

data class ReadaloudDomainModel(
    val uuid: String,
    val filepath: String,
    val missing: Int?,
    val status: String?,
    val currentStage: String?,
    val stageProgress: Int?,
    val queuePosition: Int?,
    val restartPending: Boolean?,
    val createdAt: String?,
    val updatedAt: String?,
)

