package com.retro99.auth.data.model

import com.retro99.auth.domain.model.CredentialsDomainModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CredentialsLocalModel(
    @SerialName("server_url")
    val serverUrl: String,
    @SerialName("username")
    val username: String,
    @SerialName("token")
    val token: String,
)

internal fun CredentialsLocalModel.toDomain(): CredentialsDomainModel {
    return CredentialsDomainModel(
        serverUrl = serverUrl,
        username = username,
        token = token,
    )
}

internal fun CredentialsDomainModel.toLocal(): CredentialsLocalModel {
    return CredentialsLocalModel(
        serverUrl = serverUrl,
        username = username,
        token = token,
    )
}

