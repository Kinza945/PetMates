package com.kynzai.domain.usecases

import com.kynzai.domain.common.AppResult
import com.kynzai.domain.common.toAppResult
import com.kynzai.domain.models.Response
import com.kynzai.domain.repositories.ResponseRepository
import java.util.UUID

class RespondToVacancyUseCase(
    private val responses: ResponseRepository,
) {
    suspend operator fun invoke(vacancyId: UUID): AppResult<Response> =
        responses.createResponse(vacancyId).toAppResult()
}

