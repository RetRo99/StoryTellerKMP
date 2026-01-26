package com.retro99.login.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestApiModel(
    @SerialName("usernameOrEmail")
    val usernameOrEmail: String,

    @SerialName("password")
    val password: String,
)

