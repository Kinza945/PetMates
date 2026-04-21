package com.kynzai.domain.repositories

import com.kynzai.domain.models.Project
import com.kynzai.domain.models.ProjectMember
import com.kynzai.domain.models.Vacancy
import java.util.UUID

interface ProjectRepository {
    suspend fun getAllProjects(): Result<List<Project>>
    suspend fun getProjectById(projectId: UUID): Result<Project>
    suspend fun getProjectMembers(projectId: UUID): Result<List<ProjectMember>>
    suspend fun getProjectVacancies(projectId: UUID): Result<List<Vacancy>>
}

