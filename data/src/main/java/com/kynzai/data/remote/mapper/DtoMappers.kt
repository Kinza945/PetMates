package com.kynzai.data.remote.mapper

import com.kynzai.data.remote.dto.*
import com.kynzai.domain.models.*
import kotlinx.serialization.builtins.ListSerializer
import java.time.Instant
import java.util.UUID

internal fun UserApiDto.toDomain(): User {
    val normalizedUsername = username?.takeIf { it.isNotBlank() }
    val normalizedId = id ?: userId ?: error("User response does not contain id")
    return User(
        userId = UUID.fromString(normalizedId),
        nickname = normalizedUsername ?: "user",
        username = normalizedUsername,
        email = email,
        avatarUrl = avatarUrl,
        age = age,
        gender = gender.toGender(),
        country = country,
        city = city,
        workplace = workplace,
        profileRole = status,
        description = description,
        hardSkills = hardSkills.orEmpty(),
        softSkills = softSkills.orEmpty(),
        contacts = buildList {
            telegram?.takeIf { it.isNotBlank() }?.let { add(Contact(name = "Telegram", link = it)) }
            github?.takeIf { it.isNotBlank() }?.let { add(Contact(name = "GitHub", link = it)) }
            vk?.takeIf { it.isNotBlank() }?.let { add(Contact(name = "VK", link = it)) }
            twitch?.takeIf { it.isNotBlank() }?.let { add(Contact(name = "Twitch", link = it)) }
        },
        status = status,
        telegram = telegram,
        github = github,
        vk = vk,
        twitch = twitch,
    )
}

internal fun UserProfileUpdate.toProfileUpdateDto(): ProfileUpdateRequestDto {
    val json = com.kynzai.data.network.BackendHttpClientFactory.jsonCodec()
    val contactsJson = contacts.takeIf { it.isNotEmpty() }?.let { list ->
        json.encodeToString(
            ListSerializer(ContactWireDto.serializer()),
            list.map { ContactWireDto(name = it.name, link = it.link) },
        )
    }
    return ProfileUpdateRequestDto(
        realName = realName,
        age = age,
        gender = gender.toWire().takeUnless { gender == Gender.UNSPECIFIED },
        country = country,
        city = city,
        workplace = workplace,
        profileRole = profileRole,
        description = description,
        hardSkills = hardSkills.takeIf { it.isNotEmpty() }?.joinToString(","),
        softSkills = softSkills.takeIf { it.isNotEmpty() }?.joinToString(","),
        contacts = contactsJson,
    )
}

private fun Gender.toWire(): String =
    when (this) {
        Gender.MALE -> "male"
        Gender.FEMALE -> "female"
        Gender.UNSPECIFIED -> "unspecified"
    }

private fun parseInstantOrNull(raw: String): Instant? =
    runCatching { Instant.parse(raw) }.getOrNull()

internal fun UserDto.toDomain(): User =
    User(
        userId = UUID.fromString(userId),
        nickname = nickname,
        avatarUrl = avatarUrl,
        realName = realName,
        age = age,
        gender = gender.toGender(),
        country = country,
        city = city,
        workplace = workplace,
        profileRole = profileRole,
        systemRole = systemRole.toSystemRole(),
        description = description,
        hardSkills = hardSkills,
        softSkills = softSkills,
        contacts = contacts.map { it.toDomain() },
        lastOnlineAt = lastOnlineAt?.let(Instant::parse),
        createdAt = createdAt?.let(Instant::parse),
    )

private fun ContactDto.toDomain(): Contact = Contact(name = name, link = link)

internal fun ProjectDto.toDomain(): Project =
    Project(
        projectId = UUID.fromString(projectId),
        ownerId = UUID.fromString(ownerId),
        name = name,
        shortDescription = shortDescription,
        fullDescription = fullDescription,
        status = status.toProjectStatus(),
        statusChangedAt = statusChangedAt?.let(Instant::parse),
        ratingCount = ratingCount ?: 0,
        createdAt = createdAt?.let(Instant::parse),
    )

internal fun ProjectMemberDto.toDomain(): ProjectMember =
    ProjectMember(
        memberId = UUID.fromString(memberId),
        projectId = UUID.fromString(projectId),
        userId = UUID.fromString(userId),
        role = role,
        joinedAt = joinedAt?.let(Instant::parse),
    )

internal fun VacancyDto.toDomain(): Vacancy =
    Vacancy(
        vacancyId = UUID.fromString(vacancyId),
        projectId = UUID.fromString(projectId),
        title = title,
        role = role,
        description = description,
        requiredTags = requiredTags,
        isOpen = isOpen,
        publishedAt = publishedAt?.let(Instant::parse),
    )

internal fun ResponseDto.toDomain(): Response =
    Response(
        responseId = UUID.fromString(responseId),
        userId = UUID.fromString(userId),
        vacancyId = UUID.fromString(vacancyId),
        status = status.toResponseStatus(),
        createdAt = createdAt?.let(Instant::parse),
    )

internal fun InviteDto.toDomain(): Invite =
    Invite(
        inviteId = UUID.fromString(inviteId),
        userId = UUID.fromString(userId),
        projectId = UUID.fromString(projectId),
        role = role,
        status = status.toInviteStatus(),
        createdAt = createdAt?.let(Instant::parse),
    )

internal fun NotificationDto.toDomain(): Notification =
    Notification(
        notificationId = UUID.fromString(notificationId),
        userId = UUID.fromString(userId),
        category = category.toNotificationCategory(),
        eventType = eventType,
        referenceType = referenceType.toReferenceType(),
        referenceId = UUID.fromString(referenceId),
        contextData = contextDataRaw?.let(::flattenJsonObject) ?: emptyMap(),
        isRead = isRead,
        createdAt = createdAt?.let(Instant::parse),
    )

private fun String?.toGender(): Gender =
    when (this?.lowercase()) {
        "male" -> Gender.MALE
        "female" -> Gender.FEMALE
        else -> Gender.UNSPECIFIED
    }

private fun String?.toSystemRole(): SystemRole =
    when (this?.lowercase()) {
        "admin" -> SystemRole.ADMIN
        "moderator" -> SystemRole.MODERATOR
        else -> SystemRole.USER
    }

private fun String?.toProjectStatus(): ProjectStatus =
    when (this?.lowercase()) {
        "paused" -> ProjectStatus.PAUSED
        "completed" -> ProjectStatus.COMPLETED
        else -> ProjectStatus.IN_PROGRESS
    }

private fun String?.toResponseStatus(): ResponseStatus =
    when (this?.lowercase()) {
        "accepted" -> ResponseStatus.ACCEPTED
        "rejected" -> ResponseStatus.REJECTED
        else -> ResponseStatus.PENDING
    }

private fun String?.toInviteStatus(): InviteStatus =
    when (this?.lowercase()) {
        "accepted" -> InviteStatus.ACCEPTED
        "declined" -> InviteStatus.DECLINED
        "cancelled" -> InviteStatus.CANCELLED
        else -> InviteStatus.PENDING
    }

private fun String?.toNotificationCategory(): NotificationCategory =
    when (this?.lowercase()) {
        "response" -> NotificationCategory.RESPONSE
        "invitation" -> NotificationCategory.INVITATION
        "project" -> NotificationCategory.PROJECT
        else -> NotificationCategory.PROJECT
    }

private fun String?.toReferenceType(): ReferenceType =
    when (this?.lowercase()) {
        "response" -> ReferenceType.RESPONSE
        "invitation" -> ReferenceType.INVITATION
        "project" -> ReferenceType.PROJECT
        else -> ReferenceType.PROJECT
    }

private fun flattenJsonObject(obj: org.json.JSONObject): Map<String, String> =
    buildMap {
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val v = obj.opt(key)
            put(key, v?.toString() ?: "")
        }
    }

