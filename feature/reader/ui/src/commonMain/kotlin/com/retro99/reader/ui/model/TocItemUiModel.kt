package com.retro99.reader.ui.model

/**
 * Represents a table of contents entry in the reader.
 *
 * @param href The href/link to navigate to this chapter
 * @param title The display title of the chapter
 * @param level The nesting level (0 for top-level, 1 for sub-chapters, etc.)
 * @param children Nested child entries for hierarchical TOC
 */
data class TocItemUiModel(
    val href: String,
    val title: String,
    val level: Int = 0,
    val children: List<TocItemUiModel> = emptyList(),
)

