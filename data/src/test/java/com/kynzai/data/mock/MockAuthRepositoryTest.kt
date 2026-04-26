package com.kynzai.data.mock

import com.kynzai.data.auth.AuthSessionStorage
import com.kynzai.data.repositories.mock.MockAuthRepository
import com.kynzai.domain.models.AuthSession
import com.kynzai.domain.models.LoginRequest
import com.kynzai.domain.models.RegisterRequest
import com.kynzai.domain.repositories.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MockAuthRepositoryTest {
    @Test
    fun register_creates_user_and_saves_session() {
        val data = FakeDataSource()
        val storage = InMemoryAuthSessionStorage()
        val repo: AuthRepository = MockAuthRepository(data, storage)

        runBlocking {
            val session = repo.register(
                RegisterRequest(
                    nickname = "new_user",
                    email = "new@example.com",
                    password = "password123!",
                )
            ).getOrThrow()

            assertEquals("new_user", session.nickname)
            assertEquals(session.userId, data.currentUserId)
            assertEquals(session, storage.load())
            assertTrue(data.users.any { it.userId == session.userId })
        }
    }

    @Test
    fun login_existing_user_succeeds() {
        val data = FakeDataSource()
        val storage = InMemoryAuthSessionStorage()
        val repo: AuthRepository = MockAuthRepository(data, storage)

        runBlocking {
            val session = repo.login(
                LoginRequest(
                    nicknameOrEmail = "kynzai",
                    password = "password123!",
                    rememberMe = true,
                )
            ).getOrThrow()

            assertEquals("kynzai", session.nickname)
            assertEquals(session.userId, data.currentUserId)
            assertEquals(session, storage.load())
        }
    }

    @Test
    fun login_unknown_user_returns_validation_error() {
        val data = FakeDataSource()
        val repo: AuthRepository = MockAuthRepository(data, InMemoryAuthSessionStorage())

        runBlocking {
            val result = repo.login(
                LoginRequest(
                    nicknameOrEmail = "unknown_user",
                    password = "password123!",
                    rememberMe = true,
                )
            )

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("Validation"))
        }
    }

    @Test
    fun logout_clears_saved_session() {
        val data = FakeDataSource()
        val storage = InMemoryAuthSessionStorage()
        val repo: AuthRepository = MockAuthRepository(data, storage)

        runBlocking {
            repo.login(LoginRequest("kynzai", "password123!", rememberMe = true)).getOrThrow()
            assertNotNull(storage.load())

            repo.logout().getOrThrow()

            assertNull(storage.load())
            assertNull(data.currentUserId)
        }
    }

    @Test
    fun restore_session_returns_saved_session() {
        val data = FakeDataSource()
        val storage = InMemoryAuthSessionStorage()
        val repo: AuthRepository = MockAuthRepository(data, storage)

        runBlocking {
            val saved = repo.register(
                RegisterRequest(
                    nickname = "restored_user",
                    email = "restored@example.com",
                    password = "password123!",
                )
            ).getOrThrow()
            data.currentUserId = null

            val restored = repo.restoreSession().getOrThrow()

            assertEquals(saved.userId, restored?.userId)
            assertEquals(saved.userId, data.currentUserId)
        }
    }

    private class InMemoryAuthSessionStorage : AuthSessionStorage {
        private var session: AuthSession? = null

        override fun save(session: AuthSession) {
            this.session = session
        }

        override fun load(): AuthSession? = session

        override fun clear() {
            session = null
        }
    }
}
