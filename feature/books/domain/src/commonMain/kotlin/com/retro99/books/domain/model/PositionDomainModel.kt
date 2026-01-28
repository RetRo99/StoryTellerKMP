package com.retro99.books.domain.model

data class PositionDomainModel(
    val uuid: String,
    val locator: LocatorDomainModel?,
    val timestamp: Long?,
    val createdAt: String?,
    val updatedAt: String?,
)

data class LocatorDomainModel(
    val href: String?,
    val type: String?,
    val title: String?,
    val target: Int?,
    val locations: LocationsDomainModel?,
)

data class LocationsDomainModel(
    val audioTimestampMs: Long?,
    val chapterIndex: Int?,
    val progression: Double?,
    val totalChapters: Int?,
    val totalDurationMs: Long?,
    val totalProgression: Double?,
    val position: Int?,
)

