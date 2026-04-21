package com.kynzai.data.remote.dto

import com.kynzai.data.network.optInstant
import com.kynzai.data.network.optStringOrNull
import org.json.JSONObject

data class NotificationDto(
    val notificationId: String,
    val userId: String,
    val category: String?,
    val eventType: String,
    val referenceType: String?,
    val referenceId: String,
    val contextDataRaw: JSONObject?,
    val isRead: Boolean,
    val createdAt: String?,
) {
    companion object {
        fun fromJson(obj: JSONObject): NotificationDto =
            NotificationDto(
                notificationId = obj.getString("notification_id"),
                userId = obj.getString("user_id"),
                category = obj.optStringOrNull("category"),
                eventType = obj.optString("event_type"),
                referenceType = obj.optStringOrNull("reference_type"),
                referenceId = obj.getString("reference_id"),
                contextDataRaw = obj.optJSONObject("context_data"),
                isRead = obj.optBoolean("is_read", false),
                createdAt = obj.optInstant("created_at")?.toString(),
            )
    }
}

