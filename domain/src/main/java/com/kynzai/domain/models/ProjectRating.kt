package com.kynzai.domain.models

import java.time.Instant
import java.util.UUID

data class ProjectRating(
    val ratingId: UUID,
    val projectId: UUID,
    val userId: UUID,
    val score: Int,
    val comment: String? = null,
    val createdAt: Instant? = null,
)
