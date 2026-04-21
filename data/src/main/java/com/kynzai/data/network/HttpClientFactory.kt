package com.kynzai.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object HttpClientFactory {
    fun create(config: SupabaseConfig): HttpClient {
        // Ktor-OkHttp engine uses OkHttp under the hood; tune timeouts here.
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

            install(DefaultRequest) {
                // Supabase PostgREST expects these headers.
                if (config.anonKey.isNotBlank()) {
                    header("apikey", config.anonKey)
                    header(HttpHeaders.Authorization, "Bearer ${config.anonKey}")
                }
            }
        }
    }
}

