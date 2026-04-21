package com.kynzai.data.remote.dto

import com.kynzai.data.network.optInstant
import com.kynzai.data.network.optStringOrNull
import org.json.JSONObject

data class InviteDto(
    val inviteId: String,
    val userId: String,
    val projectId: String,
    val role: String,
    val status: String?,
    val createdAt: String?,
) {
    companion object {
        fun fromJson(obj: JSONObject): InviteDto =
            InviteDto(
                inviteId = obj.getString("invite_id"),
                userId = obj.getString("user_id"),
                projectId = obj.getString("project_id"),
                role = obj.optString("role"),
                status = obj.optStringOrNull("status"),
                createdAt = obj.optInstant("created_at")?.toString(),
            )
    }
}

