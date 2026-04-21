package com.kynzai.data.remote.dto

import com.kynzai.data.network.optInstant
import com.kynzai.data.network.optStringOrNull
import com.kynzai.data.network.toStringList
import org.json.JSONArray
import org.json.JSONObject

data class UserDto(
    val userId: String,
    val nickname: String,
    val avatarUrl: String?,
    val realName: String?,
    val age: Int?,
    val gender: String?,
    val country: String?,
    val city: String?,
    val workplace: String?,
    val profileRole: String?,
    val systemRole: String?,
    val description: String?,
    val hardSkills: List<String>,
    val softSkills: List<String>,
    val contacts: List<ContactDto>,
    val lastOnlineAt: String?,
    val createdAt: String?,
) {
    companion object {
        fun fromJson(obj: JSONObject): UserDto {
            val contactsArr = obj.optJSONArray("contacts") ?: JSONArray()
            val contacts = buildList {
                for (i in 0 until contactsArr.length()) {
                    val c = contactsArr.optJSONObject(i) ?: continue
                    add(ContactDto.fromJson(c))
                }
            }

            return UserDto(
                userId = obj.getString("user_id"),
                nickname = obj.getString("nickname"),
                avatarUrl = obj.optStringOrNull("avatar_url"),
                realName = obj.optStringOrNull("real_name"),
                age = obj.optInt("age").takeIf { it != 0 } ?: obj.opt("age")?.let { (it as? Number)?.toInt() },
                gender = obj.optStringOrNull("gender"),
                country = obj.optStringOrNull("country"),
                city = obj.optStringOrNull("city"),
                workplace = obj.optStringOrNull("workplace"),
                profileRole = obj.optStringOrNull("profile_role"),
                systemRole = obj.optStringOrNull("system_role"),
                description = obj.optStringOrNull("description"),
                hardSkills = (obj.optJSONArray("hard_skills") ?: JSONArray()).toStringList(),
                softSkills = (obj.optJSONArray("soft_skills") ?: JSONArray()).toStringList(),
                contacts = contacts,
                lastOnlineAt = obj.optInstant("last_online_at")?.toString(),
                createdAt = obj.optInstant("created_at")?.toString(),
            )
        }
    }
}

data class ContactDto(
    val name: String,
    val link: String,
) {
    companion object {
        fun fromJson(obj: JSONObject): ContactDto =
            ContactDto(
                name = obj.optString("name"),
                link = obj.optString("link"),
            )
    }
}

