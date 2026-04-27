package com.kynzai.data.remote

import com.kynzai.data.network.SupabaseConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import javax.inject.Inject

class SupabaseRestApi @Inject constructor(
    private val http: HttpClient,
    private val config: SupabaseConfig,
) {
    /*
     * Тонкая обёртка над Supabase PostgREST table endpoints.
     *
     * Этот класс специально не знает о domain-моделях. Репозитории сами выбирают
     * таблицу, фильтры и JSON-тело, а затем маппят сырой JSON в DTO/domain.
     * Чем меньше логики в транспортном слое, тем проще переключение mock -> real API:
     * UI и use-case продолжают работать через контракты репозиториев.
     */
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

    suspend fun postTableJson(
        table: String,
        bodyJson: String,
        query: Map<String, String> = mapOf("select" to "*"),
    ): Result<String> {
        if (config.baseUrl.isBlank()) {
            return Result.failure(IllegalStateException("SUPABASE_URL is empty (set BuildConfig field in :data)."))
        }

        val url = config.baseUrl.trimEnd('/') + "/rest/v1/$table"
        val response = http.post(url) {
            query.forEach { (k, v) -> parameter(k, v) }
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            // Без Prefer:return=representation Supabase не вернёт созданную/обновлённую строку.
            header("Prefer", "return=representation")
            setBody(bodyJson)
        }

        return response.toJsonResult("POST", table)
    }

    suspend fun patchTableJson(
        table: String,
        bodyJson: String,
        query: Map<String, String>,
    ): Result<String> {
        if (config.baseUrl.isBlank()) {
            return Result.failure(IllegalStateException("SUPABASE_URL is empty (set BuildConfig field in :data)."))
        }

        val url = config.baseUrl.trimEnd('/') + "/rest/v1/$table"
        val response = http.patch(url) {
            query.forEach { (k, v) -> parameter(k, v) }
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            // Репозиторий сразу маппит ответ, поэтому PATCH тоже должен вернуть representation.
            header("Prefer", "return=representation")
            setBody(bodyJson)
        }

        return response.toJsonResult("PATCH", table)
    }

    suspend fun deleteTableJson(
        table: String,
        query: Map<String, String>,
    ): Result<Unit> {
        if (config.baseUrl.isBlank()) {
            return Result.failure(IllegalStateException("SUPABASE_URL is empty (set BuildConfig field in :data)."))
        }

        val url = config.baseUrl.trimEnd('/') + "/rest/v1/$table"
        val response = http.delete(url) {
            query.forEach { (k, v) -> parameter(k, v) }
            header(HttpHeaders.Accept, "application/json")
        }

        if (!response.status.isSuccess()) {
            val body = runCatching { response.body<String>() }.getOrNull()
            val details = body?.let { " | $it" }.orEmpty()
            return Result.failure(
                IllegalStateException("Supabase DELETE $table failed: HTTP ${response.status.value} ${response.status.description}$details")
            )
        }

        return Result.success(Unit)
    }

    suspend fun postRpcJson(
        functionName: String,
        bodyJson: String = "{}",
    ): Result<String> {
        if (config.baseUrl.isBlank()) {
            return Result.failure(IllegalStateException("SUPABASE_URL is empty (set BuildConfig field in :data)."))
        }

        val url = config.baseUrl.trimEnd('/') + "/rest/v1/rpc/$functionName"
        val response = http.post(url) {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(bodyJson)
        }

        return response.toJsonResult("RPC", functionName)
    }

    private suspend fun io.ktor.client.statement.HttpResponse.toJsonResult(
        method: String,
        table: String,
    ): Result<String> {
        // Пробрасываем тело ошибки сервера в Result, чтобы ViewModel могла показать понятное состояние.
        if (!status.isSuccess()) {
            val body = runCatching { body<String>() }.getOrNull()
            val details = body?.let { " | $it" }.orEmpty()
            return Result.failure(
                IllegalStateException("Supabase $method $table failed: HTTP ${status.value} ${status.description}$details")
            )
        }

        return Result.success(body())
    }
}
