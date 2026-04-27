package com.kynzai.data.repositories.mock

import com.kynzai.data.mock.FakeDataSource
import com.kynzai.domain.models.Vacancy
import com.kynzai.domain.repositories.VacancyRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class MockVacancyRepository @Inject constructor(
    private val data: FakeDataSource,
) : VacancyRepository {
    override suspend fun getVacancyById(vacancyId: UUID): Result<Vacancy> =
        data.vacancies.firstOrNull { it.vacancyId == vacancyId }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Vacancy not found: $vacancyId"))

    override suspend fun getVacanciesByProject(projectId: UUID): Result<List<Vacancy>> =
        Result.success(data.vacancies.filter { it.projectId == projectId })

    override suspend fun createVacancy(
        projectId: UUID,
        title: String,
        role: String,
        description: String,
        requiredTags: List<String>,
        isOpen: Boolean,
    ): Result<Vacancy> {
        val now = Instant.now()
        val vacancy = Vacancy(
            vacancyId = UUID.randomUUID(),
            projectId = projectId,
            title = title.trim(),
            role = role.trim(),
            description = description.trim(),
            requiredTags = requiredTags,
            isOpen = isOpen,
            publishedAt = now,
        )
        data.vacancies.add(0, vacancy)
        return Result.success(vacancy)
    }

    override suspend fun updateVacancy(
        vacancyId: UUID,
        title: String,
        role: String,
        description: String,
        requiredTags: List<String>,
        isOpen: Boolean,
    ): Result<Vacancy> {
        val idx = data.vacancies.indexOfFirst { it.vacancyId == vacancyId }
        if (idx == -1) return Result.failure(NoSuchElementException("Vacancy not found: $vacancyId"))
        val updated = data.vacancies[idx].copy(
            title = title.trim(),
            role = role.trim(),
            description = description.trim(),
            requiredTags = requiredTags,
            isOpen = isOpen,
        )
        data.vacancies[idx] = updated
        return Result.success(updated)
    }

    override suspend fun deleteVacancy(vacancyId: UUID): Result<Unit> {
        val userId = data.currentUserId ?: return Result.failure(IllegalStateException("Unauthorized"))
        val vacancy = data.vacancies.firstOrNull { it.vacancyId == vacancyId }
            ?: return Result.failure(NoSuchElementException("Vacancy not found: $vacancyId"))
        val project = data.projects.firstOrNull { it.projectId == vacancy.projectId }
            ?: return Result.failure(NoSuchElementException("Project not found: ${vacancy.projectId}"))
        if (project.ownerId != userId) {
            return Result.failure(IllegalStateException("Only project owner can delete vacancy"))
        }

        data.responses.removeAll { it.vacancyId == vacancyId }
        data.notifications.removeAll { it.referenceId == vacancyId || it.contextData["vacancy_id"] == vacancyId.toString() }
        data.vacancies.removeAll { it.vacancyId == vacancyId }
        return Result.success(Unit)
    }
}
