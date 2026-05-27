package com.kynzai.data.remote

import com.kynzai.data.network.BackendConfig
import com.kynzai.data.network.BackendHttpClientFactory
import com.kynzai.data.remote.dto.AuthResponseDto
import com.kynzai.data.remote.dto.ErrorMessageDto
import com.kynzai.data.remote.dto.LoginRequestDto
import com.kynzai.data.remote.dto.ProfileUpdateRequestDto
import com.kynzai.data.remote.dto.RegisterRequestDto
import com.kynzai.data.remote.dto.UserApiDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Named

class BackendApi @Inject constructor(
    @Named("backend") private val http: HttpClient,
    @Named("backendAuth") private val authHttp: HttpClient,
    private val config: BackendConfig,
) {
    suspend fun login(email: String, password: String): Result<AuthResponseDto> =
        safeAuthCall {
            authHttp.post(apiUrl("/api/auth/login")) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(LoginRequestDto(email = email, password = password))
            }
        }

    suspend fun register(
        nickname: String,
        email: String,
        password: String,
    ): Result<AuthResponseDto> =
        safeAuthCall {
            authHttp.post(apiUrl("/api/auth/register")) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(
                    RegisterRequestDto(
                        nickname = nickname,
                        email = email,
                        password = password,
                        confirmPassword = password,
                    )
                )
            }
        }

    suspend fun getMyProfile(): Result<UserApiDto> =
        safeCall {
            http.get(apiUrl("/api/profile/me"))
        }

    suspend fun updateMyProfile(body: ProfileUpdateRequestDto): Result<UserApiDto> =
        safeCall {
            http.put(apiUrl("/api/profile/me")) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(body)
            }
        }

    suspend fun getAllUsers(): Result<List<UserApiDto>> =
        safeCall {
            http.get(apiUrl("/api/users"))
        }

    suspend fun getUserById(userId: String): Result<UserApiDto> =
        safeCall {
            http.get(apiUrl("/api/users/$userId"))
        }

    private suspend inline fun <reified T> safeAuthCall(
        crossinline request: suspend () -> io.ktor.client.statement.HttpResponse,
    ): Result<T> = runCatching {
        requireBaseUrl()
        val response = request()
        if (!response.status.isSuccess()) {
            throw response.toApiException()
        }
        response.body<T>()
    }.mapNetworkErrors()

    private suspend inline fun <reified T> safeCall(
        crossinline request: suspend () -> io.ktor.client.statement.HttpResponse,
    ): Result<T> = runCatching {
        requireBaseUrl()
        val response = request()
        if (!response.status.isSuccess()) {
            throw response.toApiException()
        }
        response.body<T>()
    }.mapNetworkErrors()

    private fun requireBaseUrl() {
        if (config.baseUrl.isBlank()) {
            throw IllegalStateException("API_BASE_URL is empty (set BuildConfig.API_BASE_URL in :data).")
        }
    }

    private fun apiUrl(path: String): String =
        config.baseUrl.trimEnd('/') + path

    private suspend fun io.ktor.client.statement.HttpResponse.toApiException(): IOException {
        val body = runCatching { body<ErrorMessageDto>() }.getOrNull()
        val message = body?.message?.takeIf { it.isNotBlank() }
            ?: "Request failed: HTTP ${status.value} ${status.description}"
        return IOException(message)
    }

    private fun <T> Result<T>.mapNetworkErrors(): Result<T> =
        fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                Result.failure(
                    when (error) {
                        is HttpRequestTimeoutException,
                        is SocketTimeoutException,
                        -> IOException(
                            "Network timeout. Check API_BASE_URL and server availability.",
                            error,
                        )
                        else -> error
                    }
                )
            },
        )
}
