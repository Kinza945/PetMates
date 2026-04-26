package com.kynzai.domain.models

import java.util.UUID

data class AuthSession(
    val userId: UUID,
    val nickname: String,
    val email: String?,
    val accessToken: String?,
)

data class LoginRequest(
    val nicknameOrEmail: String,
    val password: String,
    val rememberMe: Boolean,
)

data class RegisterRequest(
    val nickname: String,
    val email: String,
    val password: String,
)
