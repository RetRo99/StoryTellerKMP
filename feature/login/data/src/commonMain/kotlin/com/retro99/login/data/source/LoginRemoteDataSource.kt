package com.retro99.login.data.source

import com.retro99.base.result.AppResult
import com.retro99.login.data.model.LoginRequestApiModel
import com.retro99.login.data.model.TokenResponseApiModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided
import retro99.network.api.NetworkClient
import retro99.network.api.postForm

@Factory
internal class LoginRemoteDataSource(
    @Provided private val networkClient: NetworkClient,
) : LoginRemoteSource {

    override suspend fun login(
        request: LoginRequestApiModel,
    ): AppResult<TokenResponseApiModel> {
        return networkClient.postForm(
            path = "/api/v2/token",
            formData = mapOf(
                "usernameOrEmail" to request.usernameOrEmail,
                "password" to request.password,
            ),
        )
    }
}

