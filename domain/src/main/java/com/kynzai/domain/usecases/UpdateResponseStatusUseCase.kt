package com.kynzai.domain.usecases

import com.kynzai.domain.common.AppResult
import com.kynzai.domain.common.toAppResult
import com.kynzai.domain.models.Response
import com.kynzai.domain.models.ResponseStatus
import com.kynzai.domain.repositories.ResponseRepository
import java.util.UUID

class UpdateResponseStatusUseCase(
    private val responses: ResponseRepository,
) {
    suspend operator fun invoke(responseId: UUID, status: ResponseStatus): AppResult<Response> =
        responses.updateResponseStatus(responseId, status).toAppResult()
}

