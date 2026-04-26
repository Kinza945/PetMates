package com.kynzai.data.repositories.mock

import com.kynzai.data.mock.FakeDataSource
import com.kynzai.domain.models.Notification
import com.kynzai.domain.models.NotificationCategory
import com.kynzai.domain.models.Project
import com.kynzai.domain.models.ProjectMember
import com.kynzai.domain.models.ProjectStatus
import com.kynzai.domain.models.ReferenceType
import com.kynzai.domain.models.Vacancy
import com.kynzai.domain.repositories.ProjectRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class MockProjectRepository @Inject constructor(
    private val data: FakeDataSource,
) : ProjectRepository {
    override suspend fun getAllProjects(): Result<List<Project>> =
        Result.success(data.projects.toList())

    override suspend fun getProjectById(projectId: UUID): Result<Project> =
        data.projects.firstOrNull { it.projectId == projectId }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Project not found: $projectId"))

    override suspend fun getProjectMembers(projectId: UUID): Result<List<ProjectMember>> =
        Result.success(data.projectMembers.filter { it.projectId == projectId })

    override suspend fun getProjectVacancies(projectId: UUID): Result<List<Vacancy>> =
        Result.success(data.vacancies.filter { it.projectId == projectId })

    override suspend fun createProject(
        name: String,
        shortDescription: String,
        fullDescription: String?,
        status: ProjectStatus,
    ): Result<Project> {
        val ownerId = data.currentUserId ?: return Result.failure(IllegalStateException("Unauthorized"))
        val now = Instant.now()
        val project = Project(
            projectId = UUID.randomUUID(),
            ownerId = ownerId,
            name = name.trim(),
            shortDescription = shortDescription.trim(),
            fullDescription = fullDescription?.trim()?.ifBlank { null },
            status = status,
            statusChangedAt = now,
            ratingCount = 0,
            createdAt = now,
        )
        data.projects.add(0, project)
        return Result.success(project)
    }

    override suspend fun updateProject(
        projectId: UUID,
        name: String,
        shortDescription: String,
        fullDescription: String?,
        status: ProjectStatus,
    ): Result<Project> {
        val idx = data.projects.indexOfFirst { it.projectId == projectId }
        if (idx == -1) return Result.failure(NoSuchElementException("Project not found: $projectId"))
        val now = Instant.now()
        val updated = data.projects[idx].copy(
            name = name.trim(),
            shortDescription = shortDescription.trim(),
            fullDescription = fullDescription?.trim()?.ifBlank { null },
            status = status,
            statusChangedAt = now,
        )
        data.projects[idx] = updated
        return Result.success(updated)
    }

    override suspend fun rateProject(projectId: UUID): Result<Project> {
        val userId = data.currentUserId ?: return Result.failure(IllegalStateException("Unauthorized"))
        val idx = data.projects.indexOfFirst { it.projectId == projectId }
        if (idx == -1) return Result.failure(NoSuchElementException("Project not found: $projectId"))

        /*
         * Оценка проекта пока реализована только в mock-слое.
         * На backend это лучше делать через RPC/Edge Function, потому что операция
         * должна быть атомарной: проверить "пользователь ещё не оценивал проект"
         * и увеличить rating_count без гонок.
         */
        val project = data.projects[idx]
        if (project.ownerId == userId) return Result.failure(IllegalStateException("Project owner cannot rate own project"))

        val ratingKey = userId to projectId
        if (!data.projectRatings.add(ratingKey)) {
            return Result.failure(IllegalStateException("Project already rated by this user"))
        }

        val updated = project.copy(ratingCount = project.ratingCount + 1)
        data.projects[idx] = updated

        // Уведомляем владельца о видимой активности проекта.
        data.notifications.add(
            0,
            Notification(
                notificationId = UUID.randomUUID(),
                userId = project.ownerId,
                category = NotificationCategory.PROJECT,
                eventType = "project.rated",
                referenceType = ReferenceType.PROJECT,
                referenceId = project.projectId,
                contextData = mapOf("project_name" to project.name),
                isRead = false,
                createdAt = Instant.now(),
            )
        )

        return Result.success(updated)
    }
}
