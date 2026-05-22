package com.kynzai.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequestDto(
    val nickname: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
)

@Serializable
data class AuthResponseDto(
    val userId: String,
    val accessToken: String,
    val refreshToken: String? = null,
    val email: String? = null,
)

@Serializable
data class ErrorMessageDto(
    val message: String,
)

@Serializable
data class AvatarUploadResponseDto(
    val avatarUrl: String,
)
