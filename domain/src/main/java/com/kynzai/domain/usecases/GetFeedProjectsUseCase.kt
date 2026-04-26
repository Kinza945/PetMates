package com.kynzai.domain.usecases

import com.kynzai.domain.common.AppResult
import com.kynzai.domain.common.Page
import com.kynzai.domain.common.ProjectFeedQuery
import com.kynzai.domain.common.toAppResult
import com.kynzai.domain.models.Project
import com.kynzai.domain.repositories.ProjectRepository

class GetFeedProjectsUseCase(
    private val projects: ProjectRepository,
) {
    suspend operator fun invoke(query: ProjectFeedQuery): AppResult<Page<Project>> =
        projects.getFeedProjects(query).toAppResult()
}

