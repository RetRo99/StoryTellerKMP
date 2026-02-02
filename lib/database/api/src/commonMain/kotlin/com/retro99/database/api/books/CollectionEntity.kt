package com.retro99.database.api.books

/**
 * Collection entity interface for database storage.
 */
interface CollectionEntity {
    val uuid: String
    val name: String
    val createdAt: String?
    val updatedAt: String?
}

