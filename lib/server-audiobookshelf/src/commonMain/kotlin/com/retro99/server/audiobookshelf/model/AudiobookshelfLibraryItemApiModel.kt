package com.retro99.server.audiobookshelf.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AudiobookshelfLibraryItemsResponse(
    @SerialName("results")
    val results: List<AudiobookshelfLibraryItemApiModel> = emptyList(),

    @SerialName("total")
    val total: Int = 0,

    @SerialName("limit")
    val limit: Int = 0,

    @SerialName("page")
    val page: Int = 0,
)

@Serializable
data class AudiobookshelfLibraryItemApiModel(
    @SerialName("id")
    val id: String,

    @SerialName("libraryId")
    val libraryId: String? = null,

    @SerialName("mediaType")
    val mediaType: String? = null,

    @SerialName("media")
    val media: AudiobookshelfMediaApiModel? = null,

    @SerialName("path")
    val path: String? = null,

    @SerialName("addedAt")
    val addedAt: Long? = null,

    @SerialName("updatedAt")
    val updatedAt: Long? = null,

    @SerialName("size")
    val size: Long? = null,

    @SerialName("isMissing")
    val isMissing: Boolean? = null,
)

@Serializable
data class AudiobookshelfMediaApiModel(
    @SerialName("metadata")
    val metadata: AudiobookshelfBookMetadataApiModel? = null,

    @SerialName("coverPath")
    val coverPath: String? = null,

    @SerialName("duration")
    val duration: Double? = null,

    @SerialName("size")
    val size: Long? = null,

    @SerialName("numTracks")
    val numTracks: Int? = null,

    @SerialName("numAudioFiles")
    val numAudioFiles: Int? = null,

    @SerialName("numChapters")
    val numChapters: Int? = null,

    @SerialName("tags")
    val tags: List<String> = emptyList(),

    @SerialName("ebookFile")
    val ebookFile: AudiobookshelfEbookFileApiModel? = null,

    @SerialName("ebookFormat")
    val ebookFileFormat: String? = null,

    @SerialName("audioFiles")
    val audioFiles: List<AudiobookshelfAudioFileApiModel> = emptyList(),

    @SerialName("chapters")
    val chapters: List<AudiobookshelfChapterApiModel> = emptyList(),
)

@Serializable
data class AudiobookshelfBookMetadataApiModel(
    @SerialName("title")
    val title: String,

    @SerialName("subtitle")
    val subtitle: String? = null,

    @SerialName("authorName")
    val authorName: String? = null,

    @SerialName("narratorName")
    val narratorName: String? = null,

    @SerialName("seriesName")
    val seriesName: String? = null,

    @SerialName("genres")
    val genres: List<String> = emptyList(),

    @SerialName("publishedYear")
    val publishedYear: String? = null,

    @SerialName("publishedDate")
    val publishedDate: String? = null,

    @SerialName("publisher")
    val publisher: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("isbn")
    val isbn: String? = null,

    @SerialName("asin")
    val asin: String? = null,

    @SerialName("language")
    val language: String? = null,

    @SerialName("explicit")
    val explicit: Boolean? = null,

    @SerialName("series")
    val series: List<AudiobookshelfSeriesItemApiModel> = emptyList(),
)

@Serializable
data class AudiobookshelfSeriesItemApiModel(
    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String,

    @SerialName("sequence")
    val sequence: String? = null,
)

@Serializable
data class AudiobookshelfEbookFileApiModel(
    @SerialName("ino")
    val ino: String? = null,

    @SerialName("ebookFormat")
    val ebookFormat: String? = null,

    @SerialName("metadata")
    val metadata: AudiobookshelfFileMetadataApiModel? = null,

    @SerialName("path")
    val path: String? = null,

    @SerialName("size")
    val size: Long? = null,

    @SerialName("addedAt")
    val addedAt: Long? = null,

    @SerialName("updatedAt")
    val updatedAt: Long? = null,
)

@Serializable
data class AudiobookshelfAudioFileApiModel(
    @SerialName("index")
    val index: Int? = null,

    @SerialName("ino")
    val ino: String? = null,

    @SerialName("metadata")
    val metadata: AudiobookshelfFileMetadataApiModel? = null,

    @SerialName("duration")
    val duration: Double? = null,

    @SerialName("mimeType")
    val mimeType: String? = null,

    @SerialName("codec")
    val codec: String? = null,

    @SerialName("bitRate")
    val bitRate: Int? = null,

    @SerialName("channels")
    val channels: Int? = null,

    @SerialName("trackNumFromMeta")
    val trackNumFromMeta: Int? = null,

    @SerialName("trackNumFromFilename")
    val trackNumFromFilename: Int? = null,

    @SerialName("addedAt")
    val addedAt: Long? = null,

    @SerialName("updatedAt")
    val updatedAt: Long? = null,
)

@Serializable
data class AudiobookshelfChapterApiModel(
    @SerialName("id")
    val id: Double? = null,

    @SerialName("start")
    val start: Double? = null,

    @SerialName("end")
    val end: Double? = null,

    @SerialName("title")
    val title: String? = null,
)

@Serializable
data class AudiobookshelfFileMetadataApiModel(
    @SerialName("filename")
    val filename: String? = null,

    @SerialName("ext")
    val ext: String? = null,

    @SerialName("path")
    val path: String? = null,

    @SerialName("relPath")
    val relPath: String? = null,

    @SerialName("size")
    val size: Long? = null,

    @SerialName("mtimeMs")
    val mtimeMs: Long? = null,

    @SerialName("ctimeMs")
    val ctimeMs: Long? = null,
)
