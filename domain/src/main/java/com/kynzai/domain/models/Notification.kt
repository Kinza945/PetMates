package com.kynzai.domain.models

import java.time.Instant
import java.util.UUID

data class Notification(
    val notificationId: UUID,
    val userId: UUID,
    val category: NotificationCategory,
    val eventType: String,
    val referenceType: ReferenceType,
    val referenceId: UUID,
    val contextData: Map<String, String> = emptyMap(),
    val isRead: Boolean = false,
    val createdAt: Instant? = null,
)

enum class NotificationCategory {
    RESPONSE,
    INVITATION,
    PROJECT,
}

enum class ReferenceType {
    RESPONSE,
    INVITATION,
    PROJECT,
}

