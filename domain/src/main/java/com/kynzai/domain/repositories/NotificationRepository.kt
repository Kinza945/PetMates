package com.kynzai.domain.repositories

import com.kynzai.domain.models.Notification
import com.kynzai.domain.common.NotificationsQuery
import com.kynzai.domain.common.Page
import com.kynzai.domain.common.PageToken
import java.util.UUID

interface NotificationRepository {
    suspend fun getNotificationsByUser(userId: UUID): Result<List<Notification>>
    suspend fun markAsRead(notificationId: UUID): Result<Unit>

    suspend fun getUnreadCount(userId: UUID): Result<Int> =
        getNotificationsByUser(userId).mapCatching { items -> items.count { !it.isRead } }

    /**
     * Расширенный список уведомлений с фильтрами/пагинацией. По умолчанию строится на getNotificationsByUser.
     */
    suspend fun getNotifications(query: NotificationsQuery): Result<Page<Notification>> =
        getNotificationsByUser(query.userId).mapCatching { all ->
            val filtered = all.asSequence()
                .filter { n -> query.isRead == null || n.isRead == query.isRead }
                .filter { n -> query.category == null || n.category == query.category }
                .sortedByDescending { it.createdAt }
                .toList()

            val offset = (query.page.token as? PageToken.Offset)?.value ?: 0
            val limit = query.page.limit.coerceAtLeast(1)
            val pageItems = filtered.drop(offset).take(limit)
            val nextOffset = offset + pageItems.size
            val nextToken = if (nextOffset >= filtered.size) null else PageToken.Offset(nextOffset)
            Page(items = pageItems, nextToken = nextToken)
        }

    /**
     * Опционально (для будущего): удаление уведомления.
     */
    suspend fun deleteNotification(notificationId: UUID): Result<Unit> =
        Result.failure(UnsupportedOperationException("deleteNotification is not implemented"))
}
