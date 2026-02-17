package com.retro99.books.ui.model

import com.retro99.books.domain.model.BookType
import kotlinx.serialization.Serializable

/**
 * Sealed class representing a book in the UI layer.
 * Can be either a Storyteller book (from remote API) or a local book (imported EPUB).
 */
@Serializable
sealed class BookUiModel {
    abstract val uuid: String
    abstract val title: String
    abstract val description: String?
    abstract val coverUrl: String?

    abstract val hasEbook: Boolean
    abstract val hasAudiobook: Boolean
    abstract val hasReadaloud: Boolean

    abstract val series: List<SeriesUiModel>

    abstract val statusName: String?

    abstract val authors: List<String>

    abstract val tags: List<String>

    abstract val subtitle: String?

    abstract val rating: Float?

    /**
     * Returns the file path for the given book type, or null if not available.
     */
    abstract fun filePath(bookType: BookType): String?

    /**
     * Book from the Storyteller server with full metadata and media files.
     */
    @Serializable
    data class StorytellerBook(
        override val uuid: String,
        override val title: String,
        override val description: String?,
        override val coverUrl: String?,
        override val subtitle: String?,
        override val authors: List<String>,
        override val series: List<SeriesUiModel>,
        override val tags: List<String>,
        override val statusName: String?,
        override val rating: Float?,
        override val hasEbook: Boolean,
        override val hasAudiobook: Boolean,
        override val hasReadaloud: Boolean,
        val ebookFilepath: String?,
        val audiobookFilepath: String?,
        val readaloudFilepath: String?,
    ) : BookUiModel() {
        override fun filePath(bookType: BookType): String? = when (bookType) {
            BookType.EBOOK -> ebookFilepath
            BookType.AUDIOBOOK -> audiobookFilepath
            BookType.READALOUD -> readaloudFilepath
        }
    }

    /**
     * Locally imported EPUB book.
     */
    @Serializable
    data class LocalBook(
        override val uuid: String,
        override val title: String,
        override val description: String?,
        override val coverUrl: String?,
        val author: String?,
        val filePath: String,
        val fileSize: Long,
        val importedAt: String,
        val lastOpenedAt: String?,
    ) : BookUiModel() {
        override val hasEbook: Boolean = true
        override val hasAudiobook: Boolean = false
        override val hasReadaloud: Boolean = false
        override val series: List<SeriesUiModel> = emptyList()
        override val statusName: String? = null
        override val authors: List<String> = listOfNotNull(author)
        override val tags: List<String> = emptyList()
        override val subtitle: String? = null
        override val rating: Float? = null

        override fun filePath(bookType: BookType): String? = when (bookType) {
            BookType.EBOOK -> filePath
            BookType.AUDIOBOOK -> null
            BookType.READALOUD -> null
        }
    }
}

@Serializable
data class SeriesUiModel(
    val uuid: String,
    val name: String,
    val position: Double?,
)
