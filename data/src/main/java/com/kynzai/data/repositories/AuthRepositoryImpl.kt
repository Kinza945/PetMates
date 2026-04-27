package com.kynzai.data.repositories

import com.kynzai.data.auth.AuthSessionStorage
import com.kynzai.data.remote.SupabaseAuthApi
import com.kynzai.domain.models.AuthSession
import com.kynzai.domain.models.LoginRequest
import com.kynzai.domain.models.RegisterRequest
import com.kynzai.domain.repositories.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: SupabaseAuthApi,
    private val storage: AuthSessionStorage,
) : AuthRepository {
    override suspend fun login(request: LoginRequest): Result<AuthSession> {
        val email = request.nicknameOrEmail.trim()
        if (email.isBlank() || request.password.isBlank()) {
            return validation("Email and password are required")
        }
        if (!email.contains("@")) {
            return validation("Supabase Auth currently requires email login. Nickname login needs a backend RPC.")
        }

        return authApi.signInWithEmail(email, request.password)
            .onSuccess { session ->
                if (request.rememberMe) storage.save(session) else storage.clear()
            }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthSession> {
        val nickname = request.nickname.trim()
        val email = request.email.trim()
        if (nickname.isBlank() || email.isBlank() || request.password.isBlank()) {
            return validation("Nickname, email and password are required")
        }

        return authApi.signUp(email, request.password, nickname)
            .onSuccess(storage::save)
    }

    override suspend fun logout(): Result<Unit> {
        val saved = storage.load()
        val remoteLogout = authApi.logout(saved?.accessToken)
        storage.clear()
        // Локальную сессию очищаем всегда, но ошибку сервера возвращаем для UI/логов.
        return remoteLogout
    }

    override suspend fun restoreSession(): Result<AuthSession?> {
        val saved = storage.load() ?: return Result.success(null)
        val refreshToken = saved.refreshToken
        if (refreshToken.isNullOrBlank()) {
            return Result.success(saved)
        }

        return authApi.refresh(refreshToken)
            .onSuccess(storage::save)
            .recoverCatching {
                storage.clear()
                null
            }
    }

    private fun <T> validation(message: String): Result<T> =
        Result.failure(IllegalArgumentException("Validation: $message"))
}
