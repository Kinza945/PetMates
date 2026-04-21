package com.kynzai.data.remote.dto

import com.kynzai.data.network.optInstant
import com.kynzai.data.network.optStringOrNull
import org.json.JSONObject

data class ProjectDto(
    val projectId: String,
    val ownerId: String,
    val name: String,
    val shortDescription: String,
    val fullDescription: String?,
    val status: String?,
    val statusChangedAt: String?,
    val ratingCount: Int?,
    val createdAt: String?,
) {
    companion object {
        fun fromJson(obj: JSONObject): ProjectDto =
            ProjectDto(
                projectId = obj.getString("project_id"),
                ownerId = obj.getString("owner_id"),
                name = obj.getString("name"),
                shortDescription = obj.optString("short_description"),
                fullDescription = obj.optStringOrNull("full_description"),
                status = obj.optStringOrNull("status"),
                statusChangedAt = obj.optInstant("status_changed_at")?.toString(),
                ratingCount = obj.opt("rating_count")?.let { (it as? Number)?.toInt() },
                createdAt = obj.optInstant("created_at")?.toString(),
            )
    }
}

