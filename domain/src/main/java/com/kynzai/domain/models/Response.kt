package com.kynzai.domain.models

import java.time.Instant
import java.util.UUID

data class Response(
    val responseId: UUID,
    val userId: UUID,
    val vacancyId: UUID,
    val status: ResponseStatus = ResponseStatus.PENDING,
    val createdAt: Instant? = null,
)

enum class ResponseStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
}

