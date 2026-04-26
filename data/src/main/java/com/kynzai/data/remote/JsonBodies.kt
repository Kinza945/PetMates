package com.kynzai.data.remote

import com.kynzai.domain.models.Contact
import com.kynzai.domain.models.Gender
import com.kynzai.domain.models.InviteStatus
import com.kynzai.domain.models.ProjectStatus
import com.kynzai.domain.models.ResponseStatus
import com.kynzai.domain.models.UserProfileUpdate
import org.json.JSONArray
import org.json.JSONObject

/*
 * Небольшие helper-функции для JSON-тел запросов в Supabase.
 *
 * Названия колонок лучше держать здесь, а не размазывать по репозиториям.
 * DTO-мапперы отвечают за направление server -> domain, а этот файл — только
 * за domain/update models -> wire формат PostgREST.
 */
internal fun JSONObject.putNullable(name: String, value: String?): JSONObject =
    put(name, value ?: JSONObject.NULL)

internal fun JSONObject.putNullableInt(name: String, value: Int?): JSONObject =
    put(name, value ?: JSONObject.NULL)

internal fun firstObjectFromArray(raw: String): JSONObject {
    // При Prefer:return=representation Supabase возвращает массив изменённых строк.
    val arr = JSONArray(raw)
    return arr.optJSONObject(0) ?: error("Server returned empty representation")
}

internal fun contactsJson(contacts: List<Contact>): JSONArray =
    JSONArray(
        contacts.map { contact ->
            JSONObject()
                .put("name", contact.name)
                .put("link", contact.link)
        }
    )

internal fun UserProfileUpdate.toJsonBody(): String =
    JSONObject()
        .putNullable("real_name", realName)
        .putNullableInt("age", age)
        .put("gender", gender.toWire())
        .putNullable("country", country)
        .putNullable("city", city)
        .putNullable("workplace", workplace)
        .putNullable("profile_role", profileRole)
        .putNullable("description", description)
        .put("hard_skills", JSONArray(hardSkills))
        .put("soft_skills", JSONArray(softSkills))
        .put("contacts", contactsJson(contacts))
        .toString()

internal fun Gender.toWire(): String =
    when (this) {
        Gender.MALE -> "male"
        Gender.FEMALE -> "female"
        Gender.UNSPECIFIED -> "unspecified"
    }

internal fun ProjectStatus.toWire(): String =
    when (this) {
        ProjectStatus.IN_PROGRESS -> "in_progress"
        ProjectStatus.PAUSED -> "paused"
        ProjectStatus.COMPLETED -> "completed"
    }

internal fun ResponseStatus.toWire(): String =
    when (this) {
        ResponseStatus.PENDING -> "pending"
        ResponseStatus.ACCEPTED -> "accepted"
        ResponseStatus.REJECTED -> "rejected"
    }

internal fun InviteStatus.toWire(): String =
    when (this) {
        InviteStatus.PENDING -> "pending"
        InviteStatus.ACCEPTED -> "accepted"
        InviteStatus.DECLINED -> "declined"
        InviteStatus.CANCELLED -> "cancelled"
    }
