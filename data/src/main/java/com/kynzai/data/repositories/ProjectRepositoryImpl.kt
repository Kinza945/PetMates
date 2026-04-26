package com.kynzai.data.repositories

import com.kynzai.data.remote.JSONArrayObjects
import com.kynzai.data.remote.SupabaseRestApi
import com.kynzai.data.remote.dto.ProjectDto
import com.kynzai.data.remote.dto.ProjectMemberDto
import com.kynzai.data.remote.dto.VacancyDto
import com.kynzai.data.remote.firstObjectFromArray
import com.kynzai.data.remote.mapper.toDomain
import com.kynzai.data.remote.putNullable
import com.kynzai.data.remote.toWire
import com.kynzai.domain.models.Project
import com.kynzai.domain.models.ProjectMember
import com.kynzai.domain.models.ProjectStatus
import com.kynzai.domain.models.Vacancy
import com.kynzai.domain.repositories.ProjectRepository
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

class ProjectRepositoryImpl @Inject constructor(
    private val api: SupabaseRestApi,
) : ProjectRepository {
    override suspend fun getAllProjects(): Result<List<Project>> =
        api.getTableJson(
            table = "projects",
            query = mapOf("select" to "*")
        ).mapCatching { raw ->
            val arr = JSONArray(raw)
            JSONArrayObjects(arr)
                .map { ProjectDto.fromJson(it).toDomain() }
                .toList()
        }

    override suspend fun getProjectById(projectId: UUID): Result<Project> =
        api.getTableJson(
            table = "projects",
            query = mapOf(
                "select" to "*",
                "project_id" to "eq.$projectId",
                "limit" to "1"
            )
        ).mapCatching { raw ->
            val arr = JSONArray(raw)
            val obj = arr.optJSONObject(0) ?: error("Project not found: $projectId")
            ProjectDto.fromJson(obj).toDomain()
        }

    override suspend fun getProjectMembers(projectId: UUID): Result<List<ProjectMember>> =
        api.getTableJson(
            table = "project_members",
            query = mapOf(
                "select" to "*",
                "project_id" to "eq.$projectId"
            )
        ).mapCatching { raw ->
            val arr = JSONArray(raw)
            JSONArrayObjects(arr)
                .map { ProjectMemberDto.fromJson(it).toDomain() }
                .toList()
        }

    override suspend fun getProjectVacancies(projectId: UUID): Result<List<Vacancy>> =
        api.getTableJson(
            table = "vacancies",
            query = mapOf(
                "select" to "*",
                "project_id" to "eq.$projectId"
            )
        ).mapCatching { raw ->
            val arr = JSONArray(raw)
            JSONArrayObjects(arr)
                .map { VacancyDto.fromJson(it).toDomain() }
                .toList()
        }

    override suspend fun createProject(
        name: String,
        shortDescription: String,
        fullDescription: String?,
        status: ProjectStatus,
    ): Result<Project> =
        api.postTableJson(
            table = "projects",
            bodyJson = JSONObject()
                .put("name", name)
                .put("short_description", shortDescription)
                .putNullable("full_description", fullDescription)
                .put("status", status.toWire())
                .toString(),
            query = mapOf("select" to "*")
        ).mapCatching { raw ->
            ProjectDto.fromJson(firstObjectFromArray(raw)).toDomain()
        }

    override suspend fun updateProject(
        projectId: UUID,
        name: String,
        shortDescription: String,
        fullDescription: String?,
        status: ProjectStatus,
    ): Result<Project> =
        api.patchTableJson(
            table = "projects",
            bodyJson = JSONObject()
                .put("name", name)
                .put("short_description", shortDescription)
                .putNullable("full_description", fullDescription)
                .put("status", status.toWire())
                .toString(),
            query = mapOf(
                "project_id" to "eq.$projectId",
                "select" to "*",
            )
        ).mapCatching { raw ->
            ProjectDto.fromJson(firstObjectFromArray(raw)).toDomain()
        }
}
