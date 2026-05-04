package com.kynzai.data.repositories.mock

import com.kynzai.data.mock.FakeDataSource
import com.kynzai.domain.models.Notification
import com.kynzai.domain.models.NotificationCategory
import com.kynzai.domain.models.Project
import com.kynzai.domain.models.ProjectMember
import com.kynzai.domain.models.ProjectRating
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

    override suspend fun rateProject(projectId: UUID, score: Int, comment: String?): Result<Project> {
        val userId = data.currentUserId ?: return Result.failure(IllegalStateException("Unauthorized"))
        val idx = data.projects.indexOfFirst { it.projectId == projectId }
        if (idx == -1) return Result.failure(NoSuchElementException("Project not found: $projectId"))
        if (score !in 1..5) return Result.failure(IllegalArgumentException("Rating score must be from 1 to 5"))

        /*
         * Оценка проекта пока реализована только в mock-слое.
         * На backend это лучше делать через RPC/Edge Function, потому что операция
         * должна быть атомарной: проверить "пользователь ещё не оценивал проект"
         * и увеличить rating_count без гонок.
         */
        val project = data.projects[idx]
        if (project.ownerId == userId) return Result.failure(IllegalStateException("Project owner cannot rate own project"))

        if (data.projectRatings.any { it.userId == userId && it.projectId == projectId }) {
            return Result.failure(IllegalStateException("Project already rated by this user"))
        }

        data.projectRatings.add(
            0,
            ProjectRating(
                ratingId = UUID.randomUUID(),
                projectId = projectId,
                userId = userId,
                score = score,
                comment = comment?.trim()?.ifBlank { null },
                createdAt = Instant.now(),
            )
        )

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

    override suspend fun getProjectRatings(projectId: UUID): Result<List<ProjectRating>> =
        Result.success(data.projectRatings.filter { it.projectId == projectId }.sortedByDescending { it.createdAt })

    override suspend fun deleteProject(projectId: UUID): Result<Unit> {
        val userId = data.currentUserId ?: return Result.failure(IllegalStateException("Unauthorized"))
        val project = data.projects.firstOrNull { it.projectId == projectId }
            ?: return Result.failure(NoSuchElementException("Project not found: $projectId"))
        if (project.ownerId != userId) {
            return Result.failure(IllegalStateException("Only project owner can delete project"))
        }

        val vacancyIds = data.vacancies
            .filter { it.projectId == projectId }
            .map { it.vacancyId }
            .toSet()
        data.responses.removeAll { it.vacancyId in vacancyIds }
        data.vacancies.removeAll { it.projectId == projectId }
        data.projectMembers.removeAll { it.projectId == projectId }
        data.invites.removeAll { it.projectId == projectId }
        data.notifications.removeAll { it.referenceId == projectId || it.contextData["project_id"] == projectId.toString() }
        data.projectRatings.removeAll { it.projectId == projectId }
        data.projects.removeAll { it.projectId == projectId }

        return Result.success(Unit)
    }
}
