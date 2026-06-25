package com.kynzai.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class UserApiDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("username")
    @JsonNames("nickname")
    val username: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("avatarUrl")
    @JsonNames("avatar_url", "avatar", "photoUrl", "photo_url", "imageUrl", "image_url", "picture")
    val avatarUrl: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("country")
    val country: String? = null,
    @SerialName("city")
    val city: String? = null,
    @SerialName("workplace")
    val workplace: String? = null,
    @SerialName("age")
    val age: Int? = null,
    @SerialName("gender")
    val gender: String? = null,
    @SerialName("telegram")
    val telegram: String? = null,
    @SerialName("github")
    val github: String? = null,
    @SerialName("vk")
    val vk: String? = null,
    @SerialName("twitch")
    val twitch: String? = null,
    @SerialName("hardSkills")
    @JsonNames("hard_skills")
    val hardSkills: List<String>? = null,
    @SerialName("softSkills")
    @JsonNames("soft_skills")
    val softSkills: List<String>? = null,
    @SerialName("status")
    @JsonNames("profileRole", "profile_role")
    val status: String? = null,
)

typealias UserProfileDto = UserApiDto

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
