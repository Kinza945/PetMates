package com.kynzai.domain.usecases

import com.kynzai.domain.common.AppResult
import com.kynzai.domain.common.toAppResult
import com.kynzai.domain.models.Vacancy
import com.kynzai.domain.repositories.VacancyRepository
import java.util.UUID

class CreateVacancyUseCase(
    private val vacancies: VacancyRepository,
) {
    suspend operator fun invoke(
        projectId: UUID,
        title: String,
        role: String,
        description: String,
        requiredTags: List<String>,
        isOpen: Boolean,
    ): AppResult<Vacancy> =
        vacancies.createVacancy(
            projectId = projectId,
            title = title,
            role = role,
            description = description,
            requiredTags = requiredTags,
            isOpen = isOpen,
        ).toAppResult()
}

