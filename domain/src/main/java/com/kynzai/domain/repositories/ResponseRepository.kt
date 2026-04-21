package com.kynzai.domain.repositories

import com.kynzai.domain.models.Response
import com.kynzai.domain.models.ResponseStatus
import java.util.UUID

interface ResponseRepository {
    suspend fun getResponsesByVacancy(vacancyId: UUID): Result<List<Response>>
    suspend fun createResponse(vacancyId: UUID): Result<Response>
    suspend fun updateResponseStatus(responseId: UUID, status: ResponseStatus): Result<Response>
}

