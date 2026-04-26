package com.kynzai.data.repositories

import com.kynzai.domain.models.AuthSession
import com.kynzai.domain.models.LoginRequest
import com.kynzai.domain.models.RegisterRequest
import com.kynzai.domain.repositories.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor() : AuthRepository {
    override suspend fun login(request: LoginRequest): Result<AuthSession> =
        notConnected()

    override suspend fun register(request: RegisterRequest): Result<AuthSession> =
        notConnected()

    override suspend fun logout(): Result<Unit> =
        Result.success(Unit)

    override suspend fun restoreSession(): Result<AuthSession?> =
        Result.success(null)

    private fun <T> notConnected(): Result<T> =
        Result.failure(UnsupportedOperationException("Supabase Auth is not connected yet"))
}
