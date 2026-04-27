package com.kynzai.data.remote

import com.kynzai.data.network.SupabaseConfig
import com.kynzai.domain.models.AuthSession
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import org.json.JSONObject
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class SupabaseAuthApi @Inject constructor(
    private val http: HttpClient,
    private val config: SupabaseConfig,
) {
    /*
     * Обёртка над Supabase Auth REST API.
     * Она возвращает только domain-neutral AuthSession, поэтому UI/SessionManager
     * не зависят от конкретного формата ответа Supabase.
     */
    suspend fun signInWithEmail(email: String, password: String): Result<AuthSession> =
        postAuth(
            path = "/auth/v1/token?grant_type=password",
            bodyJson = JSONObject()
                .put("email", email)
                .put("password", password)
                .toString(),
        ).mapCatching(::parseSession)

    suspend fun signUp(email: String, password: String, nickname: String): Result<AuthSession> =
        postAuth(
            path = "/auth/v1/signup",
            bodyJson = JSONObject()
                .put("email", email)
                .put("password", password)
                .put(
                    "data",
                    JSONObject()
                        .put("nickname", nickname)
                )
                .toString(),
        ).mapCatching(::parseSession)

    suspend fun refresh(refreshToken: String): Result<AuthSession> =
        postAuth(
            path = "/auth/v1/token?grant_type=refresh_token",
            bodyJson = JSONObject()
                .put("refresh_token", refreshToken)
                .toString(),
        ).mapCatching(::parseSession)

    suspend fun logout(accessToken: String?): Result<Unit> {
        if (config.baseUrl.isBlank()) {
            return Result.failure(IllegalStateException("SUPABASE_URL is empty (set BuildConfig field in :data)."))
        }

        val response = http.post(config.baseUrl.trimEnd('/') + "/auth/v1/logout") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            if (!accessToken.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            setBody("{}")
        }

        if (!response.status.isSuccess()) {
            return Result.failure(response.toAuthException("logout"))
        }

        return Result.success(Unit)
    }

    private suspend fun postAuth(path: String, bodyJson: String): Result<String> {
        if (config.baseUrl.isBlank()) {
            return Result.failure(IllegalStateException("SUPABASE_URL is empty (set BuildConfig field in :data)."))
        }

        val response = http.post(config.baseUrl.trimEnd('/') + path) {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(bodyJson)
        }

        if (!response.status.isSuccess()) {
            return Result.failure(response.toAuthException(path))
        }

        return Result.success(response.body())
    }

    private suspend fun io.ktor.client.statement.HttpResponse.toAuthException(operation: String): IllegalStateException {
        val body = runCatching { body<String>() }.getOrNull()
        val details = body?.let { " | $it" }.orEmpty()
        return IllegalStateException("Supabase Auth $operation failed: HTTP ${status.value} ${status.description}$details")
    }

    private fun parseSession(raw: String): AuthSession {
        val json = JSONObject(raw)
        val user = json.optJSONObject("user") ?: error("Supabase Auth response does not contain user")
        val userMetadata = user.optJSONObject("user_metadata")
        val email = user.optString("email").takeIf { it.isNotBlank() }
        val nickname = userMetadata?.optString("nickname")
            ?.takeIf { it.isNotBlank() }
            ?: email?.substringBefore("@")
            ?: "user"
        val expiresIn = json.optLong("expires_in", 0L).takeIf { it > 0L }

        return AuthSession(
            userId = UUID.fromString(user.getString("id")),
            nickname = nickname,
            email = email,
            accessToken = json.optString("access_token").takeIf { it.isNotBlank() },
            refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() },
            expiresAtEpochSeconds = expiresIn?.let { Instant.now().epochSecond + it },
        )
    }
}
