package com.retro99.books.ui.series.model

import com.retro99.books.domain.model.BookDomainModel
import com.retro99.books.domain.model.SeriesDomainModel

fun SeriesDomainModel.toListUiModel(
    books: List<BookDomainModel>,
): SeriesListUiModel {
    // Find books that belong to this series by matching series name
    // (series UUID from /api/v2/series doesn't match the numeric ID in books' series data)
    val seriesBooks = books.filter { book ->
        book.series.any { it.name.equals(name, ignoreCase = true) }
    }
    // Sort by position in series, then by title
    val sortedBooks = seriesBooks.sortedWith(
        compareBy(
            { book -> book.series.find { it.name.equals(name, ignoreCase = true) }?.position ?: Double.MAX_VALUE },
            { it.title },
        ),
    )
    // Get cover from first book in series
    val coverUrl = sortedBooks.firstOrNull()?.coverUrl

    return SeriesListUiModel(
        uuid = uuid,
        name = name,
        featured = featured,
        coverUrl = coverUrl,
        bookCount = seriesBooks.size,
    )
}

