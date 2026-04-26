package com.kynzai.domain.models

/**
 * Patch-модель для редактирования профиля.
 *
 * Она специально отделена от [User]: UI отправляет только редактируемые поля,
 * а серверные поля вроде userId, systemRole и createdAt остаются неизменяемыми.
 */
data class UserProfileUpdate(
    val realName: String? = null,
    val age: Int? = null,
    val gender: Gender = Gender.UNSPECIFIED,
    val country: String? = null,
    val city: String? = null,
    val workplace: String? = null,
    val profileRole: String? = null,
    val description: String? = null,
    val hardSkills: List<String> = emptyList(),
    val softSkills: List<String> = emptyList(),
    val contacts: List<Contact> = emptyList(),
)
