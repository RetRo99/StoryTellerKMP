package com.retro99.reader.domain.model

data class BookmarkDomainModel(
    val id: String,
    val bookUuid: String,
    val locatorHref: String,
    val locatorType: String?,
    val locatorTitle: String?,
    val progression: Double?,
    val totalProgression: Double?,
    val chapterIndex: Int?,
    val position: Int?,
    val createdAt: String,
    val sortOrder: Int = 0,
)
