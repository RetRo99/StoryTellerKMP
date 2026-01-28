package com.retro99.books.domain.model

data class SeriesDomainModel(
    val uuid: String,
    val name: String,
    val featured: Int?,
    val position: Int?,
    val createdAt: String?,
    val updatedAt: String?,
)

