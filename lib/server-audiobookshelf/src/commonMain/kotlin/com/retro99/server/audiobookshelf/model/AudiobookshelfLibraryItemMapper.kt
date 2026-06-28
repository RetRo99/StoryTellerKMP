package com.retro99.server.audiobookshelf.model

import com.retro99.server.api.ServerBook
import com.retro99.server.api.ServerBookSeries

fun AudiobookshelfLibraryItemApiModel.toDomain(
    serverId: String,
    baseUrl: String?,
): ServerBook {
    val metadata = media?.metadata
    val hasEbook = media?.ebookFile != null
    val hasAudiobook = media?.numAudioFiles?.let { it > 0 } ?: false

    return ServerBook(
        uuid = id,
        serverId = serverId,
        title = metadata?.title ?: "",
        description = metadata?.description,
        coverUrl = baseUrl?.let { "${it.trimEnd('/')}/api/items/$id/cover" },
        authors = listOfNotNull(metadata?.authorName),
        narrators = listOfNotNull(metadata?.narratorName),
        series = metadata?.series.orEmpty().map { seriesItem ->
            ServerBookSeries(
                id = seriesItem.id,
                name = seriesItem.name,
                sequence = seriesItem.sequence?.toFloatOrNull(),
            )
        },
        tags = media?.tags.orEmpty(),
        hasEbook = hasEbook,
        hasAudiobook = hasAudiobook,
        hasReadaloud = false,
        ebookFilepath = media?.ebookFile?.path,
        audiobookFilepath = media?.let { if (hasAudiobook) it.toString() else null },
        ebookFileSize = media?.ebookFile?.size,
        audiobookFileSize = media?.size,
        createdAt = addedAt?.toString(),
        lastOpenedAt = null,
        publicationDate = metadata?.publishedYear,
        isLocal = false,
    )
}
