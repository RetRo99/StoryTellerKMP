package com.retro99.database.api.books

/**
 * Book entity interface for database storage.
 * Normalized schema with separate tables for related entities.
 */
interface BookEntity {
    val uuid: String
    val id: Long
    val title: String
    val subtitle: String?
    val language: String?
    val publicationDate: String?
    val description: String?
    val rating: Float?
    val suffix: String?
    val statusUuid: String?
    val createdAt: String?
    val updatedAt: String?
}
