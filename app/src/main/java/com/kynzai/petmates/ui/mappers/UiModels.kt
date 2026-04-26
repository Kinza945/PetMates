package com.kynzai.petmates.ui.mappers

import androidx.compose.ui.graphics.Color
import com.kynzai.domain.models.Contact
import com.kynzai.domain.models.Project
import com.kynzai.domain.models.ProjectMember
import com.kynzai.domain.models.ProjectStatus
import com.kynzai.domain.models.Response
import com.kynzai.domain.models.ResponseStatus
import com.kynzai.domain.models.User
import com.kynzai.domain.models.Vacancy
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import java.util.UUID

data class ContactUi(
    val name: String,
    val link: String,
)

data class UserUi(
    val id: String,
    val nickname: String,
    val realName: String,
    val age: String,
    val country: String,
    val city: String,
    val workplace: String,
    val profileRole: String,
    val description: String,
    val hardSkills: List<String>,
    val softSkills: List<String>,
    val contacts: List<ContactUi>,
)

data class ProjectUi(
    val id: String,
    val ownerId: String,
    val name: String,
    val shortDescription: String,
    val fullDescription: String,
    val statusLabel: String,
    val statusColor: Color,
    val ratingText: String,
)

data class VacancyUi(
    val id: String,
    val projectId: String,
    val title: String,
    val role: String,
    val description: String,
    val requiredTags: List<String>,
    val isOpen: Boolean,
    val statusLabel: String,
)

data class MemberUi(
    val id: String,
    val userId: String,
    val role: String,
)

data class ResponseUi(
    val id: String,
    val userId: String,
    val vacancyId: String,
    val statusLabel: String,
    val isPending: Boolean,
)

fun User.toUi(): UserUi =
    UserUi(
        id = userId.toString(),
        nickname = nickname,
        realName = realName.orEmpty(),
        age = age?.let { "$it лет" }.orEmpty(),
        country = country.orEmpty(),
        city = city.orEmpty(),
        workplace = workplace.orEmpty(),
        profileRole = profileRole.orEmpty(),
        description = description.orEmpty(),
        hardSkills = hardSkills,
        softSkills = softSkills,
        contacts = contacts.map { it.toUi() },
    )

fun Contact.toUi(): ContactUi = ContactUi(name = name, link = link)

fun Project.toUi(): ProjectUi =
    ProjectUi(
        id = projectId.toString(),
        ownerId = ownerId.toString(),
        name = name,
        shortDescription = shortDescription,
        fullDescription = fullDescription.orEmpty(),
        statusLabel = status.toUiLabel(),
        statusColor = status.toUiColor(),
        ratingText = "$ratingCount оценок",
    )

fun Vacancy.toUi(): VacancyUi =
    VacancyUi(
        id = vacancyId.toString(),
        projectId = projectId.toString(),
        title = title,
        role = role,
        description = description,
        requiredTags = requiredTags,
        isOpen = isOpen,
        statusLabel = if (isOpen) "Открыта" else "Закрыта",
    )

fun ProjectMember.toUi(): MemberUi =
    MemberUi(id = memberId.toString(), userId = userId.toString(), role = role)

fun Response.toUi(): ResponseUi =
    ResponseUi(
        id = responseId.toString(),
        userId = userId.toString(),
        vacancyId = vacancyId.toString(),
        statusLabel = status.toUiLabel(),
        isPending = status == ResponseStatus.PENDING,
    )

fun ProjectStatus.toUiLabel(): String =
    when (this) {
        ProjectStatus.IN_PROGRESS -> "Активен"
        ProjectStatus.PAUSED -> "Пауза"
        ProjectStatus.COMPLETED -> "Завершён"
    }

fun ProjectStatus.toUiColor(): Color =
    when (this) {
        ProjectStatus.IN_PROGRESS -> PetMatesPrimary
        ProjectStatus.PAUSED -> Color(0xFFFFB300)
        ProjectStatus.COMPLETED -> Color(0xFF3AC83D)
    }

fun ResponseStatus.toUiLabel(): String =
    when (this) {
        ResponseStatus.PENDING -> "В ожидании"
        ResponseStatus.ACCEPTED -> "Принят"
        ResponseStatus.REJECTED -> "Отклонён"
    }

fun String.toUuidOrNull(): UUID? =
    runCatching { UUID.fromString(this) }.getOrNull()
