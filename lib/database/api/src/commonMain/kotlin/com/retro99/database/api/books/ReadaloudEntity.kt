package com.retro99.database.api.books

/**
 * Readaloud entity interface for database storage.
 */
interface ReadaloudEntity {
    val uuid: String
    val bookUuid: String
    val filepath: String?
    val missing: Int?
    val status: String?
    val currentStage: String?
    val stageProgress: Double?
    val queuePosition: Int?
    val restartPending: Boolean?
    val createdAt: String?
    val updatedAt: String?
}

