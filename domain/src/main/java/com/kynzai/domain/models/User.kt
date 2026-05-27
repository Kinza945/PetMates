package com.kynzai.domain.models

import java.time.Instant
import java.util.UUID

data class User(
    val userId: UUID,
    val id: String = userId.toString(),
    val nickname: String,
    val username: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val realName: String? = null,
    val age: Int? = null,
    val gender: Gender = Gender.UNSPECIFIED,
    val country: String? = null,
    val city: String? = null,
    val workplace: String? = null,
    val profileRole: String? = null,
    val systemRole: SystemRole = SystemRole.USER,
    val description: String? = null,
    val hardSkills: List<String> = emptyList(),
    val softSkills: List<String> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val status: String? = null,
    val telegram: String? = null,
    val github: String? = null,
    val vk: String? = null,
    val twitch: String? = null,
    val lastOnlineAt: Instant? = null,
    val createdAt: Instant? = null,
)

data class Contact(
    val name: String,
    val link: String,
)

enum class Gender {
    MALE,
    FEMALE,
    UNSPECIFIED,
}

enum class SystemRole {
    ADMIN,
    MODERATOR,
    USER,
}

