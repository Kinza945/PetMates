package com.kynzai.data.repositories

import com.kynzai.domain.common.AppConfig
import com.kynzai.domain.common.CachedResource
import com.kynzai.data.remote.JSONArrayObjects
import com.kynzai.data.remote.BackendApi
import com.kynzai.data.remote.SupabaseRestApi
import com.kynzai.data.remote.dto.UserDto
import com.kynzai.data.remote.mapper.toDomain
import com.kynzai.data.remote.mapper.toProfileUpdateDto
import com.kynzai.data.remote.objectFromRpc
import com.kynzai.data.remote.toJsonBody
import com.kynzai.domain.models.User
import com.kynzai.domain.models.UserProfileUpdate
import com.kynzai.domain.repositories.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import java.util.UUID
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: SupabaseRestApi,
    private val backendApi: BackendApi,
) : UserRepository {
    private val profileCache = MutableStateFlow<CachedResource<User>>(CachedResource())
    private var profileLastLoadedAtMs: Long = 0L

    override fun observeMyProfile(forceRefresh: Boolean): Flow<CachedResource<User>> = flow {
        val cached = profileCache.value
        val now = System.currentTimeMillis()
        val hasFreshCache = cached.data != null &&
            now - profileLastLoadedAtMs <= AppConfig.PROFILE_CACHE_TTL_MS

        if (cached.data != null) {
            emit(cached.copy(isRefreshing = false, error = null))
        }
        if (!forceRefresh && hasFreshCache) {
            return@flow
        }

        if (cached.data != null) {
            emit(cached.copy(isRefreshing = true, error = null))
        }

        backendApi.getMyProfile()
            .mapCatching { it.toDomain() }
            .onSuccess { user ->
                profileLastLoadedAtMs = System.currentTimeMillis()
                val fresh = CachedResource(data = user, isRefreshing = false)
                profileCache.value = fresh
                emit(fresh)
            }
            .onFailure { error ->
                val failed = CachedResource(
                    data = cached.data,
                    isRefreshing = false,
                    error = error,
                )
                profileCache.value = failed
                emit(failed)
            }
    }

    override suspend fun getUserById(userId: UUID): Result<User> =
        backendApi.getUserById(userId.toString())
            .mapCatching { it.toDomain() }
            .recoverCatching {
                api.getTableJson(
            table = "users",
            query = mapOf(
                "select" to "*",
                "user_id" to "eq.$userId",
                "limit" to "1"
            )
                ).getOrThrow().let { raw ->
                    val arr = JSONArray(raw)
                    val obj = arr.optJSONObject(0) ?: error("User not found: $userId")
                    UserDto.fromJson(obj).toDomain()
                }
            }

    override suspend fun getUserByNickname(nickname: String): Result<User> =
        backendApi.getAllUsers()
            .mapCatching { users ->
                users.firstOrNull { it.username.equals(nickname, ignoreCase = true) }?.toDomain()
                    ?: error("User not found: $nickname")
            }
            .recoverCatching {
                api.getTableJson(
            table = "users",
            query = mapOf(
                "select" to "*",
                "nickname" to "eq.$nickname",
                "limit" to "1"
            )
                ).getOrThrow().let { raw ->
                    val arr = JSONArray(raw)
                    val obj = arr.optJSONObject(0) ?: error("User not found: $nickname")
                    UserDto.fromJson(obj).toDomain()
                }
            }

    override suspend fun searchUsers(query: String): Result<List<User>> =
        backendApi.getAllUsers()
            .mapCatching { users ->
                val normalized = query.trim()
                users.map { it.toDomain() }
                    .filter { user ->
                        normalized.isBlank() ||
                            user.nickname.contains(normalized, ignoreCase = true) ||
                            user.hardSkills.any { it.contains(normalized, ignoreCase = true) } ||
                            user.softSkills.any { it.contains(normalized, ignoreCase = true) }
                    }
            }
            .recoverCatching {
                api.getTableJson(
            table = "users",
            query = mapOf(
                "select" to "*",
                "nickname" to "ilike.*$query*",
                "limit" to "50"
            )
                ).getOrThrow().let { raw ->
                    val arr = JSONArray(raw)
                    JSONArrayObjects(arr)
                        .map { UserDto.fromJson(it).toDomain() }
                        .toList()
                }
            }

    override suspend fun updateProfile(update: UserProfileUpdate): Result<User> =
        backendApi.updateMyProfile(update.toProfileUpdateDto())
            .mapCatching { it.toDomain() }
            .onSuccess { user ->
                profileLastLoadedAtMs = System.currentTimeMillis()
                profileCache.value = CachedResource(data = user)
            }
//            .recoverCatching {
//                api.postRpcJson(
//            functionName = "update_my_profile",
//            bodyJson = update.toJsonBody(),
//                ).getOrThrow().let { raw ->
//                    UserDto.fromJson(objectFromRpc(raw)).toDomain()
//                }
//            }

    override suspend fun deleteAccount(): Result<Unit> =
        api.postRpcJson(
            functionName = "delete_my_account",
            bodyJson = "{}",
        ).map { Unit }
}
