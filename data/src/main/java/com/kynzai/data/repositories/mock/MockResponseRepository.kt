package com.kynzai.data.repositories.mock

import com.kynzai.data.mock.FakeDataSource
import com.kynzai.domain.models.Notification
import com.kynzai.domain.models.NotificationCategory
import com.kynzai.domain.models.ReferenceType
import com.kynzai.domain.models.Response
import com.kynzai.domain.models.ResponseStatus
import com.kynzai.domain.repositories.ResponseRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class MockResponseRepository @Inject constructor(
    private val data: FakeDataSource,
) : ResponseRepository {
    override suspend fun getResponsesByVacancy(vacancyId: UUID): Result<List<Response>> =
        Result.success(data.responses.filter { it.vacancyId == vacancyId })

    override suspend fun createResponse(vacancyId: UUID): Result<Response> {
        val userId = data.currentUserId ?: return Result.failure(IllegalStateException("Unauthorized"))

        /*
         * Повторяем бизнес-правила, которые позже должен гарантировать backend:
         * - гость не может отправить отклик;
         * - автор проекта не может откликнуться на собственную вакансию;
         * - участник команды не может откликнуться на вакансию своего проекта;
         * - повторный pending-отклик запрещён, но после accepted/rejected можно откликнуться снова.
         */
        val vacancy = data.vacancies.firstOrNull { it.vacancyId == vacancyId }
            ?: return Result.failure(NoSuchElementException("Vacancy not found: $vacancyId"))
        val project = data.projects.firstOrNull { it.projectId == vacancy.projectId }
            ?: return Result.failure(NoSuchElementException("Project not found: ${vacancy.projectId}"))
        if (project.ownerId == userId) return Result.failure(IllegalStateException("Project owner cannot respond to own vacancy"))
        val alreadyMember = data.projectMembers.any { it.projectId == project.projectId && it.userId == userId }
        if (alreadyMember) return Result.failure(IllegalStateException("Project member cannot respond to project vacancy"))

        val alreadyPending = data.responses.any {
            it.vacancyId == vacancyId && it.userId == userId && it.status == ResponseStatus.PENDING
        }
        if (alreadyPending) return Result.failure(IllegalStateException("Pending response already exists"))

        val response = Response(
            responseId = UUID.randomUUID(),
            userId = userId,
            vacancyId = vacancyId,
            status = ResponseStatus.PENDING,
            createdAt = Instant.now(),
        )
        data.responses.add(0, response)

        // Автор проекта сразу получает непрочитанное уведомление о новом отклике.
        data.notifications.add(
            0,
            Notification(
                notificationId = UUID.randomUUID(),
                userId = project.ownerId,
                category = NotificationCategory.RESPONSE,
                eventType = "response.created",
                referenceType = ReferenceType.RESPONSE,
                referenceId = response.responseId,
                contextData = mapOf(
                    "project_name" to project.name,
                    "vacancy_title" to vacancy.title,
                ),
                isRead = false,
                createdAt = Instant.now(),
            )
        )

        return Result.success(response)
    }

    override suspend fun updateResponseStatus(responseId: UUID, status: ResponseStatus): Result<Response> {
        val idx = data.responses.indexOfFirst { it.responseId == responseId }
        if (idx == -1) return Result.failure(NoSuchElementException("Response not found: $responseId"))

        val updated = data.responses[idx].copy(status = status)
        data.responses[idx] = updated

        // После принятия/отклонения уведомляем автора отклика.
        val vacancy = data.vacancies.firstOrNull { it.vacancyId == updated.vacancyId }
        val project = vacancy?.let { v -> data.projects.firstOrNull { it.projectId == v.projectId } }
        data.notifications.add(
            0,
            Notification(
                notificationId = UUID.randomUUID(),
                userId = updated.userId,
                category = NotificationCategory.RESPONSE,
                eventType = when (status) {
                    ResponseStatus.ACCEPTED -> "response.accepted"
                    ResponseStatus.REJECTED -> "response.rejected"
                    ResponseStatus.PENDING -> "response.pending"
                },
                referenceType = ReferenceType.RESPONSE,
                referenceId = updated.responseId,
                contextData = buildMap {
                    if (project != null) put("project_name", project.name)
                    if (vacancy != null) put("vacancy_title", vacancy.title)
                },
                isRead = false,
                createdAt = Instant.now(),
            )
        )

        return Result.success(updated)
    }

    override suspend fun cancelResponse(responseId: UUID): Result<Unit> {
        val idx = data.responses.indexOfFirst { it.responseId == responseId }
        if (idx == -1) return Result.failure(NoSuchElementException("Response not found: $responseId"))

        val response = data.responses.removeAt(idx)
        val vacancy = data.vacancies.firstOrNull { it.vacancyId == response.vacancyId }
        val project = vacancy?.let { v -> data.projects.firstOrNull { it.projectId == v.projectId } }
        val now = Instant.now()

        if (project != null) {
            // Автор проекта должен знать, что кандидат отозвал отклик.
            data.notifications.add(
                0,
                Notification(
                    notificationId = UUID.randomUUID(),
                    userId = project.ownerId,
                    category = NotificationCategory.RESPONSE,
                    eventType = "response.cancelled",
                    referenceType = ReferenceType.RESPONSE,
                    referenceId = response.responseId,
                    contextData = buildMap {
                        put("project_name", project.name)
                        if (vacancy != null) put("vacancy_title", vacancy.title)
                    },
                    isRead = false,
                    createdAt = now,
                )
            )
        }

        // Кандидат тоже получает локальное подтверждение; это помогает тестировать уведомления и badge.
        data.notifications.add(
            0,
            Notification(
                notificationId = UUID.randomUUID(),
                userId = response.userId,
                category = NotificationCategory.RESPONSE,
                eventType = "response.cancelled",
                referenceType = ReferenceType.RESPONSE,
                referenceId = response.responseId,
                contextData = buildMap {
                    if (project != null) put("project_name", project.name)
                    if (vacancy != null) put("vacancy_title", vacancy.title)
                },
                isRead = false,
                createdAt = now,
            )
        )

        return Result.success(Unit)
    }
}
