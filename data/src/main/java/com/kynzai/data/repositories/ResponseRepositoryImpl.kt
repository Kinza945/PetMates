package com.kynzai.data.repositories

import com.kynzai.data.remote.JSONArrayObjects
import com.kynzai.data.remote.SupabaseRestApi
import com.kynzai.data.remote.dto.ResponseDto
import com.kynzai.data.remote.firstObjectFromArray
import com.kynzai.data.remote.mapper.toDomain
import com.kynzai.data.remote.objectFromRpc
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
        api.postRpcJson(
            functionName = "respond_to_vacancy",
            bodyJson = JSONObject()
                .put("p_vacancy_id", vacancyId.toString())
                .toString()
        ).mapCatching { raw ->
            ResponseDto.fromJson(objectFromRpc(raw)).toDomain()
        }

    override suspend fun updateResponseStatus(responseId: UUID, status: ResponseStatus): Result<Response> =
        api.postRpcJson(
            functionName = "update_response_status",
            bodyJson = JSONObject()
                .put("p_response_id", responseId.toString())
                .put("p_status", status.toWire())
                .toString()
        ).mapCatching { raw ->
            ResponseDto.fromJson(objectFromRpc(raw)).toDomain()
        }

    override suspend fun cancelResponse(responseId: UUID): Result<Unit> =
        api.postRpcJson(
            functionName = "cancel_response",
            bodyJson = JSONObject()
                .put("p_response_id", responseId.toString())
                .toString()
        ).map { Unit }
}
