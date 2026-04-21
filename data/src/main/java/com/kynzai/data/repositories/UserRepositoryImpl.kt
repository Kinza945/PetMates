package com.kynzai.data.repositories

import com.kynzai.data.remote.JSONArrayObjects
import com.kynzai.data.remote.SupabaseRestApi
import com.kynzai.data.remote.dto.UserDto
import com.kynzai.data.remote.mapper.toDomain
import com.kynzai.domain.models.User
import com.kynzai.domain.repositories.UserRepository
import org.json.JSONArray
import java.util.UUID
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: SupabaseRestApi,
) : UserRepository {
    override suspend fun getUserById(userId: UUID): Result<User> =
        api.getTableJson(
            table = "users",
            query = mapOf(
                "select" to "*",
                "user_id" to "eq.$userId",
                "limit" to "1"
            )
        ).mapCatching { raw ->
            val arr = JSONArray(raw)
            val obj = arr.optJSONObject(0) ?: error("User not found: $userId")
            UserDto.fromJson(obj).toDomain()
        }

    override suspend fun getUserByNickname(nickname: String): Result<User> =
        api.getTableJson(
            table = "users",
            query = mapOf(
                "select" to "*",
                "nickname" to "eq.$nickname",
                "limit" to "1"
            )
        ).mapCatching { raw ->
            val arr = JSONArray(raw)
            val obj = arr.optJSONObject(0) ?: error("User not found: $nickname")
            UserDto.fromJson(obj).toDomain()
        }

    override suspend fun searchUsers(query: String): Result<List<User>> =
        api.getTableJson(
            table = "users",
            query = mapOf(
                "select" to "*",
                "nickname" to "ilike.*$query*",
                "limit" to "50"
            )
        ).mapCatching { raw ->
            val arr = JSONArray(raw)
            JSONArrayObjects(arr)
                .map { UserDto.fromJson(it).toDomain() }
                .toList()
        }
}

