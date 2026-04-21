package com.kynzai.data.remote.dto

import com.kynzai.data.network.optInstant
import com.kynzai.data.network.toStringList
import org.json.JSONArray
import org.json.JSONObject

data class VacancyDto(
    val vacancyId: String,
    val projectId: String,
    val title: String,
    val role: String,
    val description: String,
    val requiredTags: List<String>,
    val isOpen: Boolean,
    val publishedAt: String?,
) {
    companion object {
        fun fromJson(obj: JSONObject): VacancyDto =
            VacancyDto(
                vacancyId = obj.getString("vacancy_id"),
                projectId = obj.getString("project_id"),
                title = obj.optString("title"),
                role = obj.optString("role"),
                description = obj.optString("description"),
                requiredTags = (obj.optJSONArray("required_tags") ?: JSONArray()).toStringList(),
                isOpen = obj.optBoolean("is_open", true),
                publishedAt = obj.optInstant("published_at")?.toString(),
            )
    }
}

