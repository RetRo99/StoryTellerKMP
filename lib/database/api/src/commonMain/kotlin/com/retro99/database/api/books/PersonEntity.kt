package com.retro99.database.api.books

/**
 * Person entity interface for database storage.
 * Used for authors, narrators, and creators.
 */
interface PersonEntity {
    val uuid: String
    val name: String
    val fileAs: String?
    val createdAt: String?
    val updatedAt: String?
}

