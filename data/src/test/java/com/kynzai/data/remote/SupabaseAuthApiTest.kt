package com.kynzai.data.remote

import com.kynzai.data.network.SupabaseConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseAuthApiTest {
    @Test
    fun signIn_uses_password_token_endpoint_and_maps_session() = runBlocking {
        val calls = mutableListOf<String>()
        val api = SupabaseAuthApi(
            http = mockHttp(calls, authSessionJson()),
            config = config(),
        )

        val session = api.signInWithEmail("user@example.com", "password123!").getOrThrow()

        assertTrue(calls.single().contains("/auth/v1/token?grant_type=password"))
        assertEquals(USER_ID, session.userId.toString())
        assertEquals("test_user", session.nickname)
        assertEquals("access-token", session.accessToken)
        assertEquals("refresh-token", session.refreshToken)
    }

    @Test
    fun signUp_uses_signup_endpoint() = runBlocking {
        val calls = mutableListOf<String>()
        val api = SupabaseAuthApi(
            http = mockHttp(calls, authSessionJson()),
            config = config(),
        )

        api.signUp("user@example.com", "password123!", "test_user").getOrThrow()

        assertTrue(calls.single().contains("/auth/v1/signup"))
    }

    @Test
    fun refresh_uses_refresh_token_endpoint() = runBlocking {
        val calls = mutableListOf<String>()
        val api = SupabaseAuthApi(
            http = mockHttp(calls, authSessionJson()),
            config = config(),
        )

        api.refresh("refresh-token").getOrThrow()

        assertTrue(calls.single().contains("/auth/v1/token?grant_type=refresh_token"))
    }

    @Test
    fun logout_uses_logout_endpoint() = runBlocking {
        val calls = mutableListOf<String>()
        val api = SupabaseAuthApi(
            http = mockHttp(calls, "{}"),
            config = config(),
        )

        api.logout("access-token").getOrThrow()

        assertTrue(calls.single().contains("/auth/v1/logout"))
    }

    private fun mockHttp(calls: MutableList<String>, body: String): HttpClient =
        HttpClient(
            MockEngine { request ->
                calls += request.url.toString()
                respond(
                    content = body,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        )

    private fun config() = SupabaseConfig(
        baseUrl = "https://petmates-test.supabase.co",
        anonKey = "anon-key",
    )

    private fun authSessionJson(): String =
        """
        {
          "access_token": "access-token",
          "refresh_token": "refresh-token",
          "expires_in": 3600,
          "user": {
            "id": "$USER_ID",
            "email": "user@example.com",
            "user_metadata": {
              "nickname": "test_user"
            }
          }
        }
        """.trimIndent()

    private companion object {
        const val USER_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
    }
}
