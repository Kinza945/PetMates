package com.kynzai.domain.repositories

import com.kynzai.domain.models.Vacancy
import java.util.UUID

interface VacancyRepository {
    suspend fun getVacancyById(vacancyId: UUID): Result<Vacancy>
    suspend fun getVacanciesByProject(projectId: UUID): Result<List<Vacancy>>

    /**
     * Создание вакансии (заявки) к проекту.
     */
    suspend fun createVacancy(
        projectId: UUID,
        title: String,
        role: String,
        description: String,
        requiredTags: List<String>,
        isOpen: Boolean,
    ): Result<Vacancy> =
        Result.failure(UnsupportedOperationException("createVacancy is not implemented"))

    suspend fun updateVacancy(
        vacancyId: UUID,
        title: String,
        role: String,
        description: String,
        requiredTags: List<String>,
        isOpen: Boolean,
    ): Result<Vacancy> =
        Result.failure(UnsupportedOperationException("updateVacancy is not implemented"))
}
