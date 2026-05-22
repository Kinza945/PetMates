package com.kynzai.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object BackendHttpClientFactory {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    fun create(
        authTokenProvider: AuthTokenProvider = EmptyAuthTokenProvider,
    ): HttpClient {
        val okHttp = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return HttpClient(OkHttp) {
            engine {
                preconfigured = okHttp
            }

            install(Logging) {
                level = LogLevel.INFO
            }

            install(ContentNegotiation) {
                json(json)
            }

            install(DefaultRequest) {
                val token = authTokenProvider.currentAccessToken()
                if (!token.isNullOrBlank()) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        }
    }

    fun jsonCodec(): Json = json
}
