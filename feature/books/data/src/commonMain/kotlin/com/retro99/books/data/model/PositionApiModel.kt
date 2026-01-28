package com.retro99.books.data.model

import com.retro99.books.domain.model.LocationsDomainModel
import com.retro99.books.domain.model.LocatorDomainModel
import com.retro99.books.domain.model.PositionDomainModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PositionApiModel(
    @SerialName("uuid")
    val uuid: String,

    @SerialName("locator")
    val locator: LocatorApiModel? = null,

    @SerialName("timestamp")
    val timestamp: Long? = null,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null,
)

@Serializable
data class LocatorApiModel(
    @SerialName("href")
    val href: String? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("title")
    val title: String? = null,

    @SerialName("target")
    val target: Int? = null,

    @SerialName("locations")
    val locations: LocationsApiModel? = null,
)

@Serializable
data class LocationsApiModel(
    @SerialName("audioTimestampMs")
    val audioTimestampMs: Long? = null,

    @SerialName("chapterIndex")
    val chapterIndex: Int? = null,

    @SerialName("progression")
    val progression: Double? = null,

    @SerialName("totalChapters")
    val totalChapters: Int? = null,

    @SerialName("totalDurationMs")
    val totalDurationMs: Long? = null,

    @SerialName("totalProgression")
    val totalProgression: Double? = null,

    @SerialName("position")
    val position: Int? = null,
)

fun PositionApiModel.toDomain(): PositionDomainModel {
    return PositionDomainModel(
        uuid = uuid,
        locator = locator?.toDomain(),
        timestamp = timestamp,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun LocatorApiModel.toDomain(): LocatorDomainModel {
    return LocatorDomainModel(
        href = href,
        type = type,
        title = title,
        target = target,
        locations = locations?.toDomain(),
    )
}

fun LocationsApiModel.toDomain(): LocationsDomainModel {
    return LocationsDomainModel(
        audioTimestampMs = audioTimestampMs,
        chapterIndex = chapterIndex,
        progression = progression,
        totalChapters = totalChapters,
        totalDurationMs = totalDurationMs,
        totalProgression = totalProgression,
        position = position,
    )
}

