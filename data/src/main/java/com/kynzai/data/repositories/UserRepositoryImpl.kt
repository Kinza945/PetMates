package com.kynzai.data.repositories

import com.kynzai.data.remote.BackendApi
import com.kynzai.data.remote.mapper.toDomain
import com.kynzai.data.remote.mapper.toProfileUpdateDto
import com.kynzai.domain.models.User
import com.kynzai.domain.models.UserProfileUpdate
import com.kynzai.domain.repositories.UserRepository
import java.util.UUID
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: BackendApi,
) : UserRepository {
    override suspend fun getUserById(userId: UUID): Result<User> =
        api.getUserById(userId.toString()).mapCatching { it.toDomain() }

    override suspend fun getUserByNickname(nickname: String): Result<User> =
        api.getAllUsers().mapCatching { users ->
            users.firstOrNull { it.nickname.equals(nickname, ignoreCase = true) }?.toDomain()
                ?: error("User not found: $nickname")
        }

    override suspend fun searchUsers(query: String): Result<List<User>> {
        val q = query.trim()
        return api.getAllUsers().mapCatching { users ->
            val mapped = users.map { it.toDomain() }
            if (q.isBlank()) {
                mapped
            } else {
                mapped.filter { user ->
                    user.nickname.contains(q, ignoreCase = true) ||
                        (user.profileRole?.contains(q, ignoreCase = true) == true) ||
                        user.hardSkills.any { it.contains(q, ignoreCase = true) } ||
                        user.softSkills.any { it.contains(q, ignoreCase = true) }
                }
            }
        }
    }

    override suspend fun updateProfile(update: UserProfileUpdate): Result<User> =
        api.updateMyProfile(update.toProfileUpdateDto()).mapCatching { it.toDomain() }

    override suspend fun deleteAccount(): Result<Unit> =
        Result.failure(UnsupportedOperationException("deleteAccount is not implemented on backend API"))
}
