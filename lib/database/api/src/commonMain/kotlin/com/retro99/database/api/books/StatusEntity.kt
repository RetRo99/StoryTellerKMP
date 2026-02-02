package com.retro99.database.api.books

/**
 * Status entity interface for database storage.
 */
interface StatusEntity {
    val uuid: String
    val name: String
    val createdAt: String?
    val updatedAt: String?
}

