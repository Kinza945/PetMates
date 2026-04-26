package com.kynzai.data.repositories

import com.kynzai.data.remote.SupabaseRestApi
import com.kynzai.data.remote.dto.VacancyDto
import com.kynzai.data.remote.firstObjectFromArray
import com.kynzai.data.remote.mapper.toDomain
import com.kynzai.domain.models.Vacancy
import com.kynzai.domain.repositories.VacancyRepository
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

class VacancyRepositoryImpl @Inject constructor(
    private val api: SupabaseRestApi,
) : VacancyRepository {
    override suspend fun getVacancyById(vacancyId: UUID): Result<Vacancy> =
        api.getTableJson(
            table = "vacancies",
            query = mapOf(
                "select" to "*",
                "vacancy_id" to "eq.$vacancyId",
                "limit" to "1"
            )
        ).mapCatching { raw ->
            val arr = JSONArray(raw)
            val obj = arr.optJSONObject(0) ?: error("Vacancy not found: $vacancyId")
            VacancyDto.fromJson(obj).toDomain()
        }

    override suspend fun getVacanciesByProject(projectId: UUID): Result<List<Vacancy>> =
        api.getTableJson(
            table = "vacancies",
            query = mapOf(
                "select" to "*",
                "project_id" to "eq.$projectId"
            )
        ).mapCatching { raw ->
            val arr = JSONArray(raw)
            (0 until arr.length())
                .mapNotNull { arr.optJSONObject(it) }
                .map { VacancyDto.fromJson(it).toDomain() }
        }

    override suspend fun createVacancy(
        projectId: UUID,
        title: String,
        role: String,
        description: String,
        requiredTags: List<String>,
        isOpen: Boolean,
    ): Result<Vacancy> =
        api.postTableJson(
            table = "vacancies",
            bodyJson = JSONObject()
                .put("project_id", projectId.toString())
                .put("title", title)
                .put("role", role)
                .put("description", description)
                .put("required_tags", JSONArray(requiredTags))
                .put("is_open", isOpen)
                .toString(),
            query = mapOf("select" to "*")
        ).mapCatching { raw ->
            VacancyDto.fromJson(firstObjectFromArray(raw)).toDomain()
        }

    override suspend fun updateVacancy(
        vacancyId: UUID,
        title: String,
        role: String,
        description: String,
        requiredTags: List<String>,
        isOpen: Boolean,
    ): Result<Vacancy> =
        api.patchTableJson(
            table = "vacancies",
            bodyJson = JSONObject()
                .put("title", title)
                .put("role", role)
                .put("description", description)
                .put("required_tags", JSONArray(requiredTags))
                .put("is_open", isOpen)
                .toString(),
            query = mapOf(
                "vacancy_id" to "eq.$vacancyId",
                "select" to "*",
            )
        ).mapCatching { raw ->
            VacancyDto.fromJson(firstObjectFromArray(raw)).toDomain()
        }
}
