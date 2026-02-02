package com.retro99.database.api.books

/**
 * Tag entity interface for database storage.
 */
interface TagEntity {
    val uuid: String
    val name: String
    val createdAt: String?
    val updatedAt: String?
}

