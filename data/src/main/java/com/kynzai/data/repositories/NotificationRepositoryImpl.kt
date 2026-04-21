package com.kynzai.data.repositories

import com.kynzai.data.remote.JSONArrayObjects
import com.kynzai.data.remote.SupabaseRestApi
import com.kynzai.data.remote.dto.NotificationDto
import com.kynzai.data.remote.mapper.toDomain
import com.kynzai.domain.models.Notification
import com.kynzai.domain.repositories.NotificationRepository
import org.json.JSONArray
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
        Result.failure(NotImplementedError("PATCH /notifications not implemented yet"))
}

