package com.kynzai.data.repositories

import com.kynzai.data.remote.JSONArrayObjects
import com.kynzai.data.remote.SupabaseRestApi
import com.kynzai.data.remote.dto.NotificationDto
import com.kynzai.data.remote.mapper.toDomain
import com.kynzai.domain.models.Notification
import com.kynzai.domain.repositories.NotificationRepository
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val api: SupabaseRestApi,
) : NotificationRepository {
    override suspend fun getNotificationsByUser(userId: UUID): Result<List<Notification>> =
        api.getTableJson(
            table = "notifications",
            query = mapOf(
                "select" to "*",
                "user_id" to "eq.$userId"
            )
        ).mapCatching { raw ->
            val arr = JSONArray(raw)
            JSONArrayObjects(arr)
                .map { NotificationDto.fromJson(it).toDomain() }
                .toList()
        }

    override suspend fun markAsRead(notificationId: UUID): Result<Unit> =
        api.patchTableJson(
            table = "notifications",
            bodyJson = JSONObject()
                .put("is_read", true)
                .toString(),
            query = mapOf("notification_id" to "eq.$notificationId")
        ).map { Unit }

    override suspend fun getUnreadCount(userId: UUID): Result<Int> =
        api.getTableJson(
            table = "notifications",
            query = mapOf(
                "select" to "notification_id",
                "user_id" to "eq.$userId",
                "is_read" to "eq.false"
            )
        ).mapCatching { raw ->
            JSONArray(raw).length()
        }

    override suspend fun deleteNotification(notificationId: UUID): Result<Unit> =
        api.deleteTableJson(
            table = "notifications",
            query = mapOf("notification_id" to "eq.$notificationId")
        )
}
