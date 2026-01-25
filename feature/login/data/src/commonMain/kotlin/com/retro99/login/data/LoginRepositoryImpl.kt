package com.retro99.login.data

import com.github.michaelbull.result.Ok
import com.retro99.base.result.CompletableResult
import com.retro99.login.domain.LoginRepository
import org.koin.core.annotation.Single

@Single
internal class LoginRepositoryImpl : LoginRepository {
    override suspend fun login(email: String, password: String): CompletableResult {
        // TODO: Implement login logic
        return Ok(Unit)
    }
}

