package com.kynzai.petmates.session

import com.kynzai.domain.models.AuthSession
import com.kynzai.domain.models.LoginRequest
import com.kynzai.domain.models.RegisterRequest
import com.kynzai.domain.models.SocialAuthProvider
import com.kynzai.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class SessionState(
    val isAuthorized: Boolean,
    val currentUserId: UUID?,
    val currentAccessToken: String? = null,
)

@Singleton
class SessionManager @Inject constructor(
    private val authRepository: AuthRepository,
) {
    /*
     * Единый источник состояния авторизации на уровне app-модуля.
     *
     * UI подписывается на StateFlow и не знает, откуда пришла сессия:
     * сейчас это MockAuthRepository, позже его можно заменить на Supabase Auth.
     * Важно: экраны и ViewModel должны зависеть от SessionManager/AuthRepository,
     * а не от FakeDataSource напрямую.
     */
    private val _state = MutableStateFlow(
        SessionState(
            isAuthorized = false,
            currentUserId = null,
            currentAccessToken = null,
        )
    )
    val state: StateFlow<SessionState> = _state.asStateFlow()

    suspend fun login(request: LoginRequest): Result<AuthSession> =
        authRepository.login(request).onSuccess(::applySession)

    suspend fun register(request: RegisterRequest): Result<AuthSession> =
        authRepository.register(request).onSuccess(::applySession)

    suspend fun completeOAuthSignIn(
        callbackUri: String,
        codeVerifier: String,
    ): Result<AuthSession> =
        authRepository.completeOAuthSignIn(callbackUri, codeVerifier).onSuccess(::applySession)

    suspend fun restoreSession(): Result<AuthSession?> =
        authRepository.restoreSession().onSuccess { session ->
            if (session != null) {
                applySession(session)
            } else {
                clearSession()
            }
        }

    suspend fun continueAsGuest(): Result<Unit> =
        authRepository.logout().onSuccess {
            clearSession()
        }

    suspend fun logout(): Result<Unit> = continueAsGuest()

    private fun applySession(session: AuthSession) {
        _state.value = SessionState(
            isAuthorized = true,
            currentUserId = session.userId,
            currentAccessToken = session.accessToken,
        )
    }

    private fun clearSession() {
        _state.value = SessionState(
            isAuthorized = false,
            currentUserId = null,
            currentAccessToken = null,
        )
    }
}
