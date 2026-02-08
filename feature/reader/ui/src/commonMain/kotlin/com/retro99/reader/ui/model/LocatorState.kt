package com.retro99.reader.ui.model

/**
 * Common data class representing the current reading position/locator.
 *
 * This is a platform-agnostic representation of the reading position that can be
 * used in the common interface. Platform implementations convert their native
 * locator types to this common model.
 *
 * @param href The href of the current resource
 * @param type The media type of the resource
 * @param title The title of the current section, if available
 * @param progression The progression within the resource (0.0 to 1.0)
 * @param position The position index, if available
 * @param totalProgression The total progression through the publication (0.0 to 1.0)
 */
data class LocatorState(
    val href: String,
    val type: String,
    val title: String?,
    val progression: Double?,
    val position: Int?,
    val totalProgression: Double?,
    val fragments: List<String>?,
)

