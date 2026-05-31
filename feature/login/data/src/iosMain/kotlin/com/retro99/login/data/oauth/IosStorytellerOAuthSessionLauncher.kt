package com.retro99.login.data.oauth

import com.github.michaelbull.result.Err
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IosStorytellerOAuthSessionLauncher : StorytellerOAuthSessionLauncher {

    override suspend fun requestAppToken(serverUrl: String): AppResult<String> {
        val tokenUrl = NSURL.URLWithString(serverUrl.trimEnd('/') + "/api/v2/token/app")
            ?: return Err(AppError.AuthError("Invalid Storyteller URL"))

        return StorytellerOAuthCallbackRegistry.awaitToken {
            UIApplication.sharedApplication.openURL(tokenUrl)
        }
    }
}
