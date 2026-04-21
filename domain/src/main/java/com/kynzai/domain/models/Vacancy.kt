package com.kynzai.domain.models

import java.time.Instant
import java.util.UUID

data class Vacancy(
    val vacancyId: UUID,
    val projectId: UUID,
    val title: String,
    val role: String,
    val description: String,
    val requiredTags: List<String> = emptyList(),
    val isOpen: Boolean = true,
    val publishedAt: Instant? = null,
)

