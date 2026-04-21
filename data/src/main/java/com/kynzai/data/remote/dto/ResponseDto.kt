package com.kynzai.data.remote.dto

import com.kynzai.data.network.optInstant
import com.kynzai.data.network.optStringOrNull
import org.json.JSONObject

data class ResponseDto(
    val responseId: String,
    val userId: String,
    val vacancyId: String,
    val status: String?,
    val createdAt: String?,
) {
    companion object {
        fun fromJson(obj: JSONObject): ResponseDto =
            ResponseDto(
                responseId = obj.getString("response_id"),
                userId = obj.getString("user_id"),
                vacancyId = obj.getString("vacancy_id"),
                status = obj.optStringOrNull("status"),
                createdAt = obj.optInstant("created_at")?.toString(),
            )
    }
}

