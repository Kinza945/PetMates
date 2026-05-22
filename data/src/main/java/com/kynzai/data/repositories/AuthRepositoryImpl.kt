package com.kynzai.data.repositories

import com.kynzai.data.auth.AuthSessionStorage
import com.kynzai.data.remote.BackendApi
import com.kynzai.data.remote.SupabaseAuthApi
import com.kynzai.domain.models.AuthSession
import com.kynzai.domain.models.LoginRequest
import com.kynzai.domain.models.RegisterRequest
import com.kynzai.domain.models.SocialAuthProvider
import com.kynzai.domain.repositories.AuthRepository
import java.util.UUID
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: BackendApi,
    private val oauthApi: SupabaseAuthApi,
    private val storage: AuthSessionStorage,
) : AuthRepository {
    override suspend fun login(request: LoginRequest): Result<AuthSession> {
        val email = request.nicknameOrEmail.trim()
        if (email.isBlank() || request.password.isBlank()) {
            return validation("Email and password are required")
        }
        if (!email.contains("@")) {
            return validation("Login requires email address")
        }

        return api.login(email, request.password)
            .mapCatching { response ->
                AuthSession(
                    userId = UUID.fromString(response.userId),
                    nickname = email.substringBefore("@"),
                    email = response.email ?: email,
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                )
            }
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

        return api.register(nickname, email, request.password)
            .mapCatching { response ->
                AuthSession(
                    userId = UUID.fromString(response.userId),
                    nickname = nickname,
                    email = response.email ?: email,
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                )
            }
            .onSuccess(storage::save)
    }

    override suspend fun logout(): Result<Unit> {
        storage.clear()
        return Result.success(Unit)
    }

    override suspend fun restoreSession(): Result<AuthSession?> {
        val saved = storage.load() ?: return Result.success(null)
        return Result.success(saved)
    }

    override fun buildOAuthAuthorizeUrl(
        provider: SocialAuthProvider,
        codeChallenge: String,
    ): Result<String> =
        oauthApi.buildOAuthAuthorizeUrl(provider, codeChallenge)

    override suspend fun completeOAuthSignIn(
        callbackUri: String,
        codeVerifier: String,
    ): Result<AuthSession> =
        oauthApi.completeOAuthSignIn(callbackUri, codeVerifier)
            .onSuccess(storage::save)

    private fun <T> validation(message: String): Result<T> =
        Result.failure(IllegalArgumentException("Validation: $message"))
}
