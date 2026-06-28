package com.retro99.server.audiobookshelf.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AudiobookshelfSeriesListResponse(
    @SerialName("results")
    val results: List<AudiobookshelfSeriesApiModel> = emptyList(),

    @SerialName("total")
    val total: Int = 0,

    @SerialName("limit")
    val limit: Int = 0,

    @SerialName("page")
    val page: Int = 0,
)

@Serializable
data class AudiobookshelfSeriesApiModel(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("addedAt")
    val addedAt: Long? = null,

    @SerialName("updatedAt")
    val updatedAt: Long? = null,
)

fun AudiobookshelfSeriesApiModel.toServerSeries(serverId: String): com.retro99.server.api.ServerSeries {
    return com.retro99.server.api.ServerSeries(
        uuid = id,
        serverId = serverId,
        name = name,
        featured = null,
        position = null,
        createdAt = addedAt?.toString(),
        updatedAt = updatedAt?.toString(),
    )
}
