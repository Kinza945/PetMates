package com.kynzai.data.remote.dto

import com.kynzai.data.network.optInstant
import org.json.JSONObject

data class ProjectMemberDto(
    val memberId: String,
    val projectId: String,
    val userId: String,
    val role: String,
    val joinedAt: String?,
) {
    companion object {
        fun fromJson(obj: JSONObject): ProjectMemberDto =
            ProjectMemberDto(
                memberId = obj.getString("member_id"),
                projectId = obj.getString("project_id"),
                userId = obj.getString("user_id"),
                role = obj.optString("role"),
                joinedAt = obj.optInstant("joined_at")?.toString(),
            )
    }
}

