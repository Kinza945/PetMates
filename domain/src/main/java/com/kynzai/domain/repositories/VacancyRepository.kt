package com.kynzai.domain.repositories

import com.kynzai.domain.models.Vacancy
import java.util.UUID

interface VacancyRepository {
    suspend fun getVacancyById(vacancyId: UUID): Result<Vacancy>
    suspend fun getVacanciesByProject(projectId: UUID): Result<List<Vacancy>>
}

