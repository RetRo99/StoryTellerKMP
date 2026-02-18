package com.retro99.books.domain.model

/**
 * Represents the source of a book - where it comes from and how to access its files.
 * This separates the book's content metadata from its source/location information.
 */
sealed class BookSource {
    /**
     * Whether this book is stored locally (imported file, not from a remote server).
     */
    abstract val isLocal: Boolean

    /**
     * A locally imported book file (EPUB, etc.).
     * The file is already on the device and doesn't need downloading.
     */
    data class Local(
        val filePath: String,
        val fileSize: Long,
        val importedAt: String,
        val lastOpenedAt: String?,
        val bookType: BookType,
    ) : BookSource() {
        override val isLocal: Boolean = true
    }

    /**
     * A book from a remote server that may need to be downloaded.
     */
    data class Remote(
        val serverId: String,
        val ebook: MediaFileDomainModel?,
        val audiobook: MediaFileDomainModel?,
        val readaloud: ReadaloudDomainModel?,
    ) : BookSource() {
        override val isLocal: Boolean = false

        val hasEbook: Boolean get() = ebook?.filepath != null
        val hasAudiobook: Boolean get() = audiobook?.filepath != null
        val hasReadaloud: Boolean get() = readaloud?.filepath != null
    }
}

