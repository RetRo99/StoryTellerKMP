package com.retro99.database.api.books

/**
 * Series entity interface for database storage.
 */
interface SeriesEntity {
    val uuid: String
    val name: String
    val featured: Int?
    val createdAt: String?
    val updatedAt: String?
}

/**
 * Book-Series relationship with position.
 */
interface BookSeriesEntity {
    val bookUuid: String
    val seriesUuid: String
    val position: Int?
}

