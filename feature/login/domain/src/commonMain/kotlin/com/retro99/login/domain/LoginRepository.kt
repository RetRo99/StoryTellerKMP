package com.retro99.login.domain

import com.retro99.base.result.CompletableResult

interface LoginRepository {
    suspend fun login(email: String, password: String): CompletableResult
}

