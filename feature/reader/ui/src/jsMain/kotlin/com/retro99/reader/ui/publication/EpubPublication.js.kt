package com.retro99.reader.ui.publication

import com.retro99.books.domain.model.BookType
import com.retro99.reader.ui.model.TocItemUiModel

actual class EpubPublication {
    actual val serverId: String = ""
    actual val bookUuid: String = ""
    actual val bookType: BookType = BookType.EBOOK
    actual val hasMediaOverlays: Boolean = false
    actual val tableOfContents: List<TocItemUiModel> = emptyList()
}
