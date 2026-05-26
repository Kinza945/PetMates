package com.kynzai.data.remote

import com.kynzai.data.network.SupabaseConfig
import com.kynzai.domain.models.AuthSession
import com.kynzai.domain.models.SocialAuthProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import org.json.JSONObject
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class SupabaseAuthApi @Inject constructor(
    private val http: HttpClient,
    private val config: SupabaseConfig,
) {
    companion object {
        //const val OAUTH_REDIRECT_URI = "com.kynzai.petmates://auth-callback"
        const val OAUTH_REDIRECT_URI = "petmates://callback"
    }
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

    fun buildOAuthAuthorizeUrl(
        provider: SocialAuthProvider,
        codeChallenge: String,
    ): Result<String> = runCatching {
        if (config.baseUrl.isBlank()) {
            throw IllegalStateException("SUPABASE_URL is empty (set BuildConfig field in :data).")
        }
        if (config.anonKey.isBlank()) {
            throw IllegalStateException("SUPABASE_ANON_KEY is empty (set BuildConfig field in :data).")
        }

        val base = config.baseUrl.trimEnd('/')
        val redirect = java.net.URLEncoder.encode(OAUTH_REDIRECT_URI, StandardCharsets.UTF_8.name())
        val challenge = java.net.URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8.name())
        val apiKey = java.net.URLEncoder.encode(config.anonKey, StandardCharsets.UTF_8.name())
        "$base/auth/v1/authorize" +
            "?provider=${provider.toSupabaseProvider()}" +
            "&redirect_to=$redirect" +
            "&code_challenge=$challenge" +
            "&code_challenge_method=s256" +
            "&apikey=$apiKey"
    }

    suspend fun completeOAuthSignIn(
        callbackUri: String,
        codeVerifier: String,
    ): Result<AuthSession> = runCatching {
        if (config.baseUrl.isBlank()) {
            throw IllegalStateException("SUPABASE_URL is empty (set BuildConfig field in :data).")
        }

        val params = parseOAuthCallback(callbackUri)
        val error = params["error"]
        if (!error.isNullOrBlank()) {
            val description = params["error_description"] ?: error
            throw IllegalStateException(description)
        }

        val authCode = params["code"]?.takeIf { it.isNotBlank() }
        val accessToken = params["access_token"]?.takeIf { it.isNotBlank() }

        when {
            !authCode.isNullOrBlank() -> exchangePkceCode(authCode, codeVerifier)
            !accessToken.isNullOrBlank() -> sessionFromImplicitTokens(
                accessToken = accessToken,
                refreshToken = params["refresh_token"],
                expiresIn = params["expires_in"]?.toLongOrNull(),
            )
            else -> throw IllegalStateException("OAuth callback does not contain code or access_token")
        }
    }

    private suspend fun exchangePkceCode(authCode: String, codeVerifier: String): AuthSession {
        val response = http.post(config.baseUrl.trimEnd('/') + "/auth/v1/token?grant_type=pkce") {
            header("apikey", config.anonKey)
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                JSONObject()
                    .put("auth_code", authCode)
                    .put("code_verifier", codeVerifier)
                    .toString(),
            )
        }
        if (!response.status.isSuccess()) {
            throw response.toAuthException("pkce")
        }
        return parseSession(response.body<String>())
    }

    private suspend fun sessionFromImplicitTokens(
        accessToken: String,
        refreshToken: String?,
        expiresIn: Long?,
    ): AuthSession {
        val user = fetchAuthUser(accessToken)
        return userToSession(
            user = user,
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = expiresIn,
        )
    }

    private fun userToSession(
        user: JSONObject,
        accessToken: String,
        refreshToken: String?,
        expiresIn: Long?,
    ): AuthSession {
        val userMetadata = user.optJSONObject("user_metadata")
        val email = user.optString("email").takeIf { it.isNotBlank() }
        val nickname = userMetadata?.optString("nickname")
            ?.takeIf { it.isNotBlank() }
            ?: userMetadata?.optString("full_name")?.takeIf { it.isNotBlank() }
            ?: userMetadata?.optString("name")?.takeIf { it.isNotBlank() }
            ?: userMetadata?.optString("user_name")?.takeIf { it.isNotBlank() }
            ?: userMetadata?.optString("preferred_username")?.takeIf { it.isNotBlank() }
            ?: email?.substringBefore("@")
            ?: "user"

        return AuthSession(
            userId = UUID.fromString(user.getString("id")),
            nickname = nickname,
            email = email,
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtEpochSeconds = expiresIn?.let { Instant.now().epochSecond + it },
        )
    }

    private suspend fun fetchAuthUser(accessToken: String): JSONObject {
        val response = http.get(config.baseUrl.trimEnd('/') + "/auth/v1/user") {
            header("apikey", config.anonKey)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        if (!response.status.isSuccess()) {
            throw response.toAuthException("user")
        }
        return JSONObject(response.body<String>())
    }

    private fun parseOAuthCallback(callbackUri: String): Map<String, String> {
        val uri = URI(callbackUri)
        val raw = buildString {
            uri.rawQuery?.let {
                append(it)
                append('&')
            }
            uri.rawFragment?.let { append(it) }
        }.trimEnd('&')

        if (raw.isBlank()) {
            throw IllegalStateException("OAuth callback URL is empty")
        }

        return raw.split('&')
            .mapNotNull { part ->
                val idx = part.indexOf('=')
                if (idx <= 0) return@mapNotNull null
                val key = URLDecoder.decode(part.substring(0, idx), StandardCharsets.UTF_8.name())
                val value = URLDecoder.decode(part.substring(idx + 1), StandardCharsets.UTF_8.name())
                key to value
            }
            .toMap()
    }

    private fun SocialAuthProvider.toSupabaseProvider(): String =
        when (this) {
            SocialAuthProvider.GOOGLE -> "google"
            SocialAuthProvider.GITHUB -> "github"
            SocialAuthProvider.TWITCH -> "twitch"
        }

    suspend fun logout(accessToken: String?): Result<Unit> = runCatching {
        if (config.baseUrl.isBlank()) {
            throw IllegalStateException("SUPABASE_URL is empty (set BuildConfig field in :data).")
        }

        val response = http.post(config.baseUrl.trimEnd('/') + "/auth/v1/logout") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            if (!accessToken.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            setBody("{}")
        }

        if (!response.status.isSuccess()) {
            throw response.toAuthException("logout")
        }
    }

    private suspend fun postAuth(path: String, bodyJson: String): Result<String> = runCatching {
        if (config.baseUrl.isBlank()) {
            throw IllegalStateException("SUPABASE_URL is empty (set BuildConfig field in :data).")
        }

        val response = http.post(config.baseUrl.trimEnd('/') + path) {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(bodyJson)
        }

        if (!response.status.isSuccess()) {
            throw response.toAuthException(path)
        }

        response.body<String>()
    }

    private suspend fun io.ktor.client.statement.HttpResponse.toAuthException(operation: String): IllegalStateException {
        val body = runCatching { body<String>() }.getOrNull()
        val details = body?.let { " | $it" }.orEmpty()
        return IllegalStateException("Supabase Auth $operation failed: HTTP ${status.value} ${status.description}$details")
    }

    private fun parseSession(raw: String): AuthSession {
        val json = JSONObject(raw)
        val user = json.optJSONObject("user") ?: error("Supabase Auth response does not contain user")
        val expiresIn = json.optLong("expires_in", 0L).takeIf { it > 0L }
        return userToSession(
            user = user,
            accessToken = json.getString("access_token"),
            refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() },
            expiresIn = expiresIn,
        )
    }
}
