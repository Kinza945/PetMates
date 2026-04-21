package com.kynzai.domain.repositories

import com.kynzai.domain.models.Notification
import java.util.UUID

interface NotificationRepository {
    suspend fun getNotificationsByUser(userId: UUID): Result<List<Notification>>
    suspend fun markAsRead(notificationId: UUID): Result<Unit>
}

