package com.retro99.auth.domain.model

data class CredentialsDomainModel(
    val serverUrl: String,
    val username: String,
    val token: String,
)

