package com.retro99.reader.domain.model

/**
 * Represents an initial reading position for restoring the reader state.
 * This is used when opening a book to restore the last reading position.
 */
data class InitialLocatorDomainModel(
    val href: String,
    val type: String,
    val title: String? = null,
    val progression: Double? = null,
    val position: Int? = null,
    val totalProgression: Double? = null,
)

