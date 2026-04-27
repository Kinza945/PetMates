package com.kynzai.domain.repositories

import com.kynzai.domain.models.Project
import com.kynzai.domain.models.ProjectMember
import com.kynzai.domain.models.Vacancy
import com.kynzai.domain.common.Page
import com.kynzai.domain.common.PageToken
import com.kynzai.domain.common.ProjectFeedQuery
import com.kynzai.domain.common.SortOrder
import com.kynzai.domain.models.ProjectStatus
import java.util.UUID

interface ProjectRepository {
    suspend fun getAllProjects(): Result<List<Project>>
    suspend fun getProjectById(projectId: UUID): Result<Project>
    suspend fun getProjectMembers(projectId: UUID): Result<List<ProjectMember>>
    suspend fun getProjectVacancies(projectId: UUID): Result<List<Vacancy>>

    /**
     * Создание проекта (для формы "добавления пета").
     */
    suspend fun createProject(
        name: String,
        shortDescription: String,
        fullDescription: String?,
        status: ProjectStatus,
    ): Result<Project> =
        Result.failure(UnsupportedOperationException("createProject is not implemented"))

    /**
     * Редактирование проекта (минимум ключевых полей).
     */
    suspend fun updateProject(
        projectId: UUID,
        name: String,
        shortDescription: String,
        fullDescription: String?,
        status: ProjectStatus,
    ): Result<Project> =
        Result.failure(UnsupportedOperationException("updateProject is not implemented"))

    /**
     * Оценка проекта чувствительна к конкурентности: real backend должен атомарно
     * защищать её от повторных голосов. Mock-слой реализует это локально для UI-сценариев.
     */
    suspend fun rateProject(projectId: UUID): Result<Project> =
        Result.failure(UnsupportedOperationException("rateProject requires backend RPC"))

    suspend fun deleteProject(projectId: UUID): Result<Unit> =
        Result.failure(UnsupportedOperationException("deleteProject is not implemented"))

    /**
     * Контракт под ленту (feed). По умолчанию реализован на базе getAllProjects() (клиентская пагинация).
     * Когда появится серверный эндпоинт, реализацию можно перенести в data без изменения UI/use-cases.
     */
    suspend fun getFeedProjects(query: ProjectFeedQuery): Result<Page<Project>> =
        getAllProjects().mapCatching { all ->
            val q = query.q?.trim().orEmpty()
            val filtered = all.asSequence()
                .filter { p -> q.isBlank() || p.name.contains(q, ignoreCase = true) }
                .filter { p -> query.status == null || p.status == query.status }
                .filter { p ->
                    if (query.tags.isEmpty()) true
                    else query.tags.any { tag ->
                        p.shortDescription.contains(tag.removePrefix("#"), ignoreCase = true) ||
                            (p.fullDescription?.contains(tag.removePrefix("#"), ignoreCase = true) == true)
                    }
                }
                .toList()

            val sorted = when (query.sort) {
                SortOrder.CREATED_AT_DESC -> filtered.sortedByDescending { it.createdAt }
                SortOrder.CREATED_AT_ASC -> filtered.sortedBy { it.createdAt }
                SortOrder.RATING_DESC -> filtered.sortedByDescending { it.ratingCount }
                else -> filtered.sortedByDescending { it.createdAt }
            }

            val offset = (query.page.token as? PageToken.Offset)?.value ?: 0
            val limit = query.page.limit.coerceAtLeast(1)
            val pageItems = sorted.drop(offset).take(limit)
            val nextOffset = offset + pageItems.size
            val nextToken = if (nextOffset >= sorted.size) null else PageToken.Offset(nextOffset)
            Page(items = pageItems, nextToken = nextToken)
        }
}
