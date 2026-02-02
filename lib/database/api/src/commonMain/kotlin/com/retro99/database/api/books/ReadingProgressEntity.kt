package com.retro99.database.api.books

/**
 * Reading progress entity interface for database storage.
 * Stores the current reading position and progress for a book.
 */
interface ReadingProgressEntity {
    val bookUuid: String
    val locatorHref: String?
    val locatorType: String?
    val locatorTitle: String?
    val progression: Double?
    val totalProgression: Double?
    val chapterIndex: Int?
    val totalChapters: Int?
    val audioTimestampMs: Long?
    val lastReadAt: String
}

