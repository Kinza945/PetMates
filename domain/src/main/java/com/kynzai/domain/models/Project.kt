package com.kynzai.domain.models

import java.time.Instant
import java.util.UUID

data class Project(
    val projectId: UUID,
    val ownerId: UUID,
    val name: String,
    val shortDescription: String,
    val fullDescription: String? = null,
    val status: ProjectStatus = ProjectStatus.IN_PROGRESS,
    val statusChangedAt: Instant? = null,
    val ratingCount: Int = 0,
    val createdAt: Instant? = null,
)

enum class ProjectStatus {
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
}

