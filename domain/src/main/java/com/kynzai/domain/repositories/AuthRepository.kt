package com.kynzai.domain.repositories

import com.kynzai.domain.models.AuthSession
import com.kynzai.domain.models.LoginRequest
import com.kynzai.domain.models.RegisterRequest
import com.kynzai.domain.models.SocialAuthProvider

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<AuthSession>
    suspend fun register(request: RegisterRequest): Result<AuthSession>
    suspend fun logout(): Result<Unit>
    suspend fun restoreSession(): Result<AuthSession?>

    fun buildOAuthAuthorizeUrl(
        provider: SocialAuthProvider,
        codeChallenge: String,
    ): Result<String>

    suspend fun completeOAuthSignIn(
        callbackUri: String,
        codeVerifier: String,
    ): Result<AuthSession>
}
