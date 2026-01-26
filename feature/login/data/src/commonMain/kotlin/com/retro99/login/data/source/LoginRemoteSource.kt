package com.retro99.login.data.source

import com.retro99.base.result.AppResult
import com.retro99.login.data.model.LoginRequestApiModel
import com.retro99.login.data.model.TokenResponseApiModel

interface LoginRemoteSource {

    suspend fun login(request: LoginRequestApiModel): AppResult<TokenResponseApiModel>
}

