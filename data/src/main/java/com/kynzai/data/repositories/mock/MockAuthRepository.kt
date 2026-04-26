package com.kynzai.data.repositories.mock

import com.kynzai.data.auth.AuthSessionStorage
import com.kynzai.data.mock.FakeDataSource
import com.kynzai.domain.models.AuthSession
import com.kynzai.domain.models.Gender
import com.kynzai.domain.models.LoginRequest
import com.kynzai.domain.models.RegisterRequest
import com.kynzai.domain.models.SystemRole
import com.kynzai.domain.models.User
import com.kynzai.domain.repositories.AuthRepository
import java.time.Instant
import java.util.UUID

class MockAuthRepository(
    private val data: FakeDataSource,
    private val storage: AuthSessionStorage,
) : AuthRepository {
    override suspend fun login(request: LoginRequest): Result<AuthSession> {
        val login = request.nicknameOrEmail.trim()
        if (login.isBlank() || request.password.isBlank()) {
            return validation("Login and password are required")
        }

        val user = data.users.firstOrNull { user ->
            user.nickname.equals(login, ignoreCase = true)
        } ?: return validation("User not found")

        val session = user.toSession(email = null)
        data.currentUserId = user.userId

        if (request.rememberMe) {
            storage.save(session)
        } else {
            storage.clear()
        }

        return Result.success(session)
    }

    override suspend fun register(request: RegisterRequest): Result<AuthSession> {
        val nickname = request.nickname.trim()
        val email = request.email.trim()
        if (nickname.isBlank() || email.isBlank() || request.password.isBlank()) {
            return validation("Nickname, email and password are required")
        }
        if (data.users.any { it.nickname.equals(nickname, ignoreCase = true) }) {
            return validation("User already exists")
        }

        val user = createUser(
            userId = UUID.randomUUID(),
            nickname = nickname,
            email = email,
            description = "Пользователь создан локально (мок-регистрация). Email: $email",
        )
        data.users.add(0, user)

        val session = user.toSession(email = email)
        data.currentUserId = user.userId
        storage.save(session)

        return Result.success(session)
    }

    override suspend fun logout(): Result<Unit> {
        data.currentUserId = null
        storage.clear()
        return Result.success(Unit)
    }

    override suspend fun restoreSession(): Result<AuthSession?> {
        val saved = storage.load()
        if (saved == null) {
            data.currentUserId = null
            return Result.success(null)
        }

        val user = data.users.firstOrNull { it.userId == saved.userId }
            ?: createUser(
                userId = saved.userId,
                nickname = saved.nickname,
                email = saved.email,
                description = "Пользователь восстановлен из локальной mock-сессии.",
            ).also { data.users.add(0, it) }

        val restored = saved.copy(
            userId = user.userId,
            nickname = user.nickname,
        )
        data.currentUserId = restored.userId
        storage.save(restored)

        return Result.success(restored)
    }

    private fun createUser(
        userId: UUID,
        nickname: String,
        email: String?,
        description: String,
    ): User {
        val now = Instant.now()
        return User(
            userId = userId,
            nickname = nickname,
            avatarUrl = null,
            realName = null,
            age = null,
            gender = Gender.UNSPECIFIED,
            country = null,
            city = null,
            workplace = null,
            profileRole = null,
            systemRole = SystemRole.USER,
            description = description,
            hardSkills = emptyList(),
            softSkills = emptyList(),
            contacts = emptyList(),
            lastOnlineAt = now,
            createdAt = now,
        )
    }

    private fun User.toSession(email: String?): AuthSession =
        AuthSession(
            userId = userId,
            nickname = nickname,
            email = email,
            accessToken = "mock-token-$userId",
        )

    private fun <T> validation(message: String): Result<T> =
        Result.failure(IllegalArgumentException("Validation: $message"))
}
