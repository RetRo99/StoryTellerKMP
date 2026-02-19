package com.retro99.server.storyteller.model

import com.retro99.server.api.ServerSeries
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * API model for series returned from the /api/v2/series endpoint.
 * This is different from StorytellerSeriesApiModel in StorytellerBookApiModel.kt
 * which represents series embedded in book responses.
 */
@Serializable
data class StorytellerSeriesListApiModel(
    @SerialName("uuid")
    val uuid: String,

    @SerialName("name")
    val name: String,

    @SerialName("featured")
    val featured: Int? = null,

    @SerialName("position")
    val position: Double? = null,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null,
)

fun StorytellerSeriesListApiModel.toServerSeries(serverId: String): ServerSeries {
    return ServerSeries(
        uuid = uuid,
        serverId = serverId,
        name = name,
        featured = featured,
        position = position,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

