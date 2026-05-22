package com.kynzai.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class UserProfileDto(
    val email: String? = null,
    val userId: String,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val realName: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val country: String? = null,
    val city: String? = null,
    val workplace: String? = null,
    val profileRole: String? = null,
    val systemRole: String? = null,
    val description: String? = null,
    val hardSkills: JsonElement? = null,
    val softSkills: JsonElement? = null,
    val contacts: JsonElement? = null,
    val lastOnlineAt: String? = null,
    val createdAt: String? = null,
    val isOnline: Boolean? = null,
    val lastSeen: String? = null,
)

@Serializable
data class ProfileUpdateRequestDto(
    val nickname: String? = null,
    val realName: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val country: String? = null,
    val city: String? = null,
    val workplace: String? = null,
    val profileRole: String? = null,
    val description: String? = null,
    val hardSkills: String? = null,
    val softSkills: String? = null,
    val contacts: String? = null,
    val avatarUrl: String? = null,
)

@Serializable
data class ContactWireDto(
    val name: String,
    val link: String,
)
