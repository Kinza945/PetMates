package com.kynzai.data.repositories

import com.kynzai.data.remote.JSONArrayObjects
import com.kynzai.data.remote.SupabaseRestApi
import com.kynzai.data.remote.dto.ResponseDto
import com.kynzai.data.remote.mapper.toDomain
import com.kynzai.domain.models.Response
import com.kynzai.domain.models.ResponseStatus
import com.kynzai.domain.repositories.ResponseRepository
import org.json.JSONArray
import java.util.UUID
import javax.inject.Inject

class ResponseRepositoryImpl @Inject constructor(
    private val api: SupabaseRestApi,
) : ResponseRepository {
    override suspend fun getResponsesByVacancy(vacancyId: UUID): Result<List<Response>> =
        api.getTableJson(
            table = "responses",
            query = mapOf(
                "select" to "*",
                "vacancy_id" to "eq.$vacancyId"
            )
        ).mapCatching { raw ->
            val arr = JSONArray(raw)
            JSONArrayObjects(arr)
                .map { ResponseDto.fromJson(it).toDomain() }
                .toList()
        }

    override suspend fun createResponse(vacancyId: UUID): Result<Response> =
        Result.failure(NotImplementedError("POST /responses not implemented yet"))

    override suspend fun updateResponseStatus(responseId: UUID, status: ResponseStatus): Result<Response> =
        Result.failure(NotImplementedError("PATCH /responses not implemented yet"))
}

