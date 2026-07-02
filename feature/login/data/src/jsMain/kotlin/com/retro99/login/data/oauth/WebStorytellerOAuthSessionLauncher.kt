package com.retro99.login.data.oauth

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult

class WebStorytellerOAuthSessionLauncher : StorytellerOAuthSessionLauncher {
    override suspend fun requestAppToken(serverUrl: String): AppResult<String> {
        return Err(AppError.NotFoundError("OAuth not supported on web yet"))
    }
}
