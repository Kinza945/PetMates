package com.kynzai.petmates.ui.profile

import com.kynzai.domain.models.Gender
import com.kynzai.domain.models.User

fun User.displayName(): String =
    username?.takeIf { it.isNotBlank() } ?: nickname

fun User.formattedAge(): String? =
    age?.let { "$it ${ageSuffix(it)}" }

fun User.formattedCity(): String? =
    city?.takeIf { it.isNotBlank() }?.let { "г. $it" }

fun User.formattedCountry(): String? =
    country?.takeIf { it.isNotBlank() }

fun User.formattedWorkplace(): String? =
    workplace?.takeIf { it.isNotBlank() }

fun User.formattedGender(): String? =
    when (gender) {
        Gender.MALE -> "Мужской"
        Gender.FEMALE -> "Женский"
        Gender.UNSPECIFIED -> null
    }

fun User.formattedStatus(): String =
    status?.takeIf { it.isNotBlank() } ?: profileRole?.takeIf { it.isNotBlank() } ?: "Роль не указана"

fun User.profileContacts(): List<Pair<String, String>> =
    buildList {
        email?.takeIf { it.isNotBlank() }?.let { add("Email" to it) }
        telegram?.takeIf { it.isNotBlank() }?.let { add("Telegram" to it) }
        github?.takeIf { it.isNotBlank() }?.let { add("GitHub" to it) }
        vk?.takeIf { it.isNotBlank() }?.let { add("VK" to it) }
        twitch?.takeIf { it.isNotBlank() }?.let { add("Twitch" to it) }
        contacts.forEach { contact ->
            if (contact.name.isNotBlank() && contact.link.isNotBlank()) {
                add(contact.name to contact.link)
            }
        }
    }.distinct()

private fun ageSuffix(age: Int): String {
    val mod100 = age % 100
    val mod10 = age % 10
    return when {
        mod100 in 11..14 -> "лет"
        mod10 == 1 -> "год"
        mod10 in 2..4 -> "года"
        else -> "лет"
    }
}

