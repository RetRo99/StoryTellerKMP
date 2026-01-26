package com.retro99.login.domain.model

data class LoginCredentialsDomainModel(
    val serverUrl: String,
    val username: String,
    val token: String,
)

