package com.kynzai.data.remote

import com.kynzai.data.network.SupabaseConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import javax.inject.Inject

class SupabaseRestApi @Inject constructor(
    private val http: HttpClient,
    private val config: SupabaseConfig,
) {
    suspend fun getTableJson(table: String, query: Map<String, String> = mapOf("select" to "*")): Result<String> {
        if (config.baseUrl.isBlank()) {
            return Result.failure(IllegalStateException("SUPABASE_URL is empty (set BuildConfig field in :data)."))
        }

        val url = config.baseUrl.trimEnd('/') + "/rest/v1/$table"
        val response = http.get(url) {
            query.forEach { (k, v) -> parameter(k, v) }
        }

        if (!response.status.isSuccess()) {
            val body = runCatching { response.body<String>() }.getOrNull()
            val details = body?.let { " | $it" }.orEmpty()
            return Result.failure(
                IllegalStateException("Supabase GET $table failed: HTTP ${response.status.value} ${response.status.description}$details")
            )
        }

        return Result.success(response.body())
    }
}
