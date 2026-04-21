package com.kynzai.domain.models

import java.time.Instant
import java.util.UUID

data class ProjectMember(
    val memberId: UUID,
    val projectId: UUID,
    val userId: UUID,
    val role: String,
    val joinedAt: Instant? = null,
)

