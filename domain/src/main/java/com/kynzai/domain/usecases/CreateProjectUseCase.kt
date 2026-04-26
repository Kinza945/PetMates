package com.kynzai.domain.usecases

import com.kynzai.domain.common.AppResult
import com.kynzai.domain.common.toAppResult
import com.kynzai.domain.models.Project
import com.kynzai.domain.models.ProjectStatus
import com.kynzai.domain.repositories.ProjectRepository

class CreateProjectUseCase(
    private val projects: ProjectRepository,
) {
    suspend operator fun invoke(
        name: String,
        shortDescription: String,
        fullDescription: String?,
        status: ProjectStatus,
    ): AppResult<Project> =
        projects.createProject(
            name = name,
            shortDescription = shortDescription,
            fullDescription = fullDescription,
            status = status,
        ).toAppResult()
}

