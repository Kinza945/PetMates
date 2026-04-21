package com.kynzai.domain.repositories

import com.kynzai.domain.models.User
import java.util.UUID

interface UserRepository {
    suspend fun getUserById(userId: UUID): Result<User>
    suspend fun getUserByNickname(nickname: String): Result<User>
    suspend fun searchUsers(query: String): Result<List<User>>
}

