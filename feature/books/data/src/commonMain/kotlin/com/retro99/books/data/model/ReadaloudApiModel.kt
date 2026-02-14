package com.retro99.books.data.model

import com.retro99.books.domain.model.ReadaloudDomainModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReadaloudApiModel(
    @SerialName("uuid")
    val uuid: String,

    @SerialName("filepath")
    val filepath: String? = null,

    @SerialName("missing")
    val missing: Int? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("currentStage")
    val currentStage: String? = null,

    @SerialName("stageProgress")
    val stageProgress: Double? = null,

    @SerialName("queuePosition")
    val queuePosition: Int? = null,

    @SerialName("restartPending")
    val restartPending: Boolean? = null,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null,
)

fun ReadaloudApiModel.toDomain(): ReadaloudDomainModel {
    return ReadaloudDomainModel(
        uuid = uuid,
        filepath = filepath,
        missing = missing,
        status = status,
        currentStage = currentStage,
        stageProgress = stageProgress,
        queuePosition = queuePosition,
        restartPending = restartPending,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

