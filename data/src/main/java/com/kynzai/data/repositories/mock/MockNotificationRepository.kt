package com.kynzai.data.repositories.mock

import com.kynzai.data.mock.FakeDataSource
import com.kynzai.domain.models.Notification
import com.kynzai.domain.repositories.NotificationRepository
import java.util.UUID
import javax.inject.Inject

class MockNotificationRepository @Inject constructor(
    private val data: FakeDataSource,
) : NotificationRepository {
    override suspend fun getNotificationsByUser(userId: UUID): Result<List<Notification>> =
        Result.success(
            data.notifications
                .filter { it.userId == userId }
                .sortedByDescending { it.createdAt }
        )

    override suspend fun markAsRead(notificationId: UUID): Result<Unit> {
        val idx = data.notifications.indexOfFirst { it.notificationId == notificationId }
        if (idx == -1) return Result.failure(NoSuchElementException("Notification not found: $notificationId"))
        data.notifications[idx] = data.notifications[idx].copy(isRead = true)
        return Result.success(Unit)
    }

    override suspend fun getUnreadCount(userId: UUID): Result<Int> =
        Result.success(data.notifications.count { it.userId == userId && !it.isRead })

    override suspend fun deleteNotification(notificationId: UUID): Result<Unit> {
        val removed = data.notifications.removeIf { it.notificationId == notificationId }
        return if (removed) Result.success(Unit) else Result.failure(NoSuchElementException("Notification not found: $notificationId"))
    }
}
