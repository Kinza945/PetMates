package com.kynzai.domain.usecases

import com.kynzai.domain.common.AppResult
import com.kynzai.domain.common.toDomainError
import com.kynzai.domain.models.Project
import com.kynzai.domain.models.ProjectMember
import com.kynzai.domain.models.User
import com.kynzai.domain.models.Vacancy
import com.kynzai.domain.repositories.ProjectRepository
import com.kynzai.domain.repositories.UserRepository
import java.util.UUID

data class ProjectDetails(
    val project: Project,
    val owner: User,
    val members: List<ProjectMember>,
    val vacancies: List<Vacancy>,
)

class GetProjectDetailsUseCase(
    private val projects: ProjectRepository,
    private val users: UserRepository,
) {
    suspend operator fun invoke(projectId: UUID): AppResult<ProjectDetails> {
        val project = projects.getProjectById(projectId).getOrElse { return AppResult.Error(it.toDomainError()) }
        val owner = users.getUserById(project.ownerId).getOrElse { return AppResult.Error(it.toDomainError()) }
        val members = projects.getProjectMembers(projectId).getOrElse { return AppResult.Error(it.toDomainError()) }
        val vacancies = projects.getProjectVacancies(projectId).getOrElse { return AppResult.Error(it.toDomainError()) }
        return AppResult.Success(ProjectDetails(project, owner, members, vacancies))
    }
}
