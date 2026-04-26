package com.kynzai.data.repositories

import com.kynzai.data.remote.JSONArrayObjects
import com.kynzai.data.remote.SupabaseRestApi
import com.kynzai.data.remote.dto.ResponseDto
import com.kynzai.data.remote.firstObjectFromArray
import com.kynzai.data.remote.mapper.toDomain
import com.kynzai.data.remote.toWire
import com.kynzai.domain.models.Response
import com.kynzai.domain.models.ResponseStatus
import com.kynzai.domain.repositories.ResponseRepository
import org.json.JSONArray
import org.json.JSONObject
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
        api.postTableJson(
            table = "responses",
            bodyJson = JSONObject()
                .put("vacancy_id", vacancyId.toString())
                .put("status", ResponseStatus.PENDING.toWire())
                .toString(),
            query = mapOf("select" to "*")
        ).mapCatching { raw ->
            ResponseDto.fromJson(firstObjectFromArray(raw)).toDomain()
        }

    override suspend fun updateResponseStatus(responseId: UUID, status: ResponseStatus): Result<Response> =
        api.patchTableJson(
            table = "responses",
            bodyJson = JSONObject()
                .put("status", status.toWire())
                .toString(),
            query = mapOf(
                "response_id" to "eq.$responseId",
                "select" to "*",
            )
        ).mapCatching { raw ->
            ResponseDto.fromJson(firstObjectFromArray(raw)).toDomain()
        }

    override suspend fun cancelResponse(responseId: UUID): Result<Unit> =
        api.deleteTableJson(
            table = "responses",
            query = mapOf("response_id" to "eq.$responseId")
        )
}
