package com.retro99.server.storyteller.source

import com.retro99.base.url.CoverUrlBuilder
import com.retro99.database.api.books.BookEntity
import com.retro99.database.api.books.CollectionEntity
import com.retro99.database.api.books.MediaFileEntity
import com.retro99.database.api.books.PersonEntity
import com.retro99.database.api.books.ReadaloudEntity
import com.retro99.database.api.books.SeriesWithPositionEntity
import com.retro99.database.api.books.StatusEntity
import com.retro99.database.api.books.TagEntity
import com.retro99.server.api.ServerBook
import com.retro99.server.api.ServerBookSeries

/**
 * Simple BookEntity implementation for caching ServerBook data.
 */
internal data class ServerBookEntityImpl(
    override val uuid: String,
    override val serverId: String,
    override val id: Long,
    override val title: String,
    override val subtitle: String?,
    override val language: String?,
    override val publicationDate: String?,
    override val description: String?,
    override val rating: Float?,
    override val suffix: String?,
    override val createdAt: String?,
    override val updatedAt: String?,
    override val authors: List<PersonEntity>,
    override val narrators: List<PersonEntity>,
    override val creators: List<PersonEntity>,
    override val series: List<SeriesWithPositionEntity>,
    override val tags: List<TagEntity>,
    override val collections: List<CollectionEntity>,
    override val status: StatusEntity?,
    override val ebook: MediaFileEntity?,
    override val audiobook: MediaFileEntity?,
    override val readaloud: ReadaloudEntity?,
) : BookEntity

internal data class SimplePersonEntity(
    override val uuid: String,
    override val name: String,
    override val fileAs: String?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : PersonEntity

internal data class SimpleSeriesEntity(
    override val uuid: String,
    override val name: String,
    override val featured: Int?,
    override val position: Double?,
    override val createdAt: String?,
    override val updatedAt: String?,
) : SeriesWithPositionEntity

internal data class SimpleTagEntity(
    override val uuid: String,
    override val name: String,
    override val createdAt: String?,
    override val updatedAt: String?,
) : TagEntity

/**
 * Convert ServerBook to BookEntity for caching.
 */
internal fun ServerBook.toEntity(): BookEntity {
    return ServerBookEntityImpl(
        uuid = uuid,
        serverId = serverId,
        id = 0L, // ServerBook doesn't have numeric id
        title = title,
        subtitle = null,
        language = null,
        publicationDate = null,
        description = description,
        rating = null,
        suffix = null,
        createdAt = null,
        updatedAt = null,
        authors = authors.map { name ->
            SimplePersonEntity(
                uuid = name, // Use name as uuid
                name = name,
                fileAs = null,
                createdAt = null,
                updatedAt = null,
            )
        },
        narrators = narrators.map { name ->
            SimplePersonEntity(
                uuid = name,
                name = name,
                fileAs = null,
                createdAt = null,
                updatedAt = null,
            )
        },
        creators = emptyList(),
        series = series.map { s ->
            SimpleSeriesEntity(
                uuid = s.id ?: s.name, // Use name as fallback uuid when id is null
                name = s.name,
                featured = null,
                position = s.sequence?.toDouble(),
                createdAt = null,
                updatedAt = null,
            )
        },
        tags = tags.map { tagName ->
            SimpleTagEntity(
                uuid = tagName, // Use name as uuid
                name = tagName,
                createdAt = null,
                updatedAt = null,
            )
        },
        collections = emptyList(),
        status = null,
        ebook = if (hasEbook) SimpleMediaFileEntity(uuid, "ebook") else null,
        audiobook = if (hasAudiobook) SimpleMediaFileEntity(uuid, "audiobook") else null,
        readaloud = if (hasReadaloud) SimpleReadaloudEntity(uuid) else null,
    )
}

internal data class SimpleMediaFileEntity(
    override val bookUuid: String,
    override val type: String,
) : MediaFileEntity {
    override val uuid: String = "$bookUuid-$type"
    override val filepath: String? = null
    override val missing: Int? = null
    override val createdAt: String? = null
    override val updatedAt: String? = null
}

internal data class SimpleReadaloudEntity(
    override val bookUuid: String,
) : ReadaloudEntity {
    override val uuid: String = "$bookUuid-readaloud"
    override val filepath: String? = "readaloud"
    override val missing: Int? = null
    override val status: String? = null
    override val currentStage: String? = null
    override val stageProgress: Double? = null
    override val queuePosition: Int? = null
    override val restartPending: Boolean? = null
    override val createdAt: String? = null
    override val updatedAt: String? = null
}

/**
 * Convert BookEntity to ServerBook for retrieval.
 */
internal fun BookEntity.toServerBook(baseUrl: String?): ServerBook {
    return ServerBook(
        uuid = uuid,
        serverId = serverId,
        title = title,
        description = description,
        coverUrl = CoverUrlBuilder.buildCoverUrl(baseUrl, uuid),
        authors = authors.map { it.name },
        narrators = narrators.map { it.name },
        series = series.map { s ->
            ServerBookSeries(
                id = s.uuid,
                name = s.name,
                sequence = s.position?.toFloat(),
            )
        },
        tags = tags.map { it.name },
        hasEbook = ebook != null,
        hasAudiobook = audiobook != null,
        hasReadaloud = readaloud != null,
    )
}

