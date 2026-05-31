package com.retro99.login.data.oauth

import com.retro99.base.result.AppResult

interface StorytellerOAuthSessionLauncher {
    suspend fun requestAppToken(serverUrl: String): AppResult<String>
}
