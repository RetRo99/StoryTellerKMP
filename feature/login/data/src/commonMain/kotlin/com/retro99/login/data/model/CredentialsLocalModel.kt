package com.retro99.login.data.model

import com.retro99.login.domain.model.LoginCredentialsDomainModel
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

internal fun CredentialsLocalModel.toDomain(): LoginCredentialsDomainModel {
    return LoginCredentialsDomainModel(
        serverUrl = serverUrl,
        username = username,
        token = token,
    )
}

internal fun LoginCredentialsDomainModel.toLocal(): CredentialsLocalModel {
    return CredentialsLocalModel(
        serverUrl = serverUrl,
        username = username,
        token = token,
    )
}

