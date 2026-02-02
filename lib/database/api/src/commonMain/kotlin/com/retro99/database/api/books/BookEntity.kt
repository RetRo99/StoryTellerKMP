package com.retro99.database.api.books

/**
 * Book entity interface for database storage.
 *
 * Only queryable fields are defined as columns.
 * The full book data is stored as a JSON blob for easy retrieval.
 */
interface BookEntity {
    // Primary key
    val uuid: String

    // Queryable fields (indexed for search/filter)
    val title: String
    val id: Long
    val rating: Float?

    // Full book data as JSON blob
    val dataJson: String
}

