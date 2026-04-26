package com.kynzai.domain.common

/**
 * Единый контракт результата для use-cases, чтобы UI не зависел от исключений и разных форматов ошибок.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: DomainError) : AppResult<Nothing>
}

sealed interface DomainError {
    data object Unauthorized : DomainError
    data object Forbidden : DomainError
    data object NotFound : DomainError
    data class Validation(val message: String? = null) : DomainError
    data class Network(val message: String? = null) : DomainError
    data class Unknown(val message: String? = null) : DomainError
}

fun <T> Result<T>.toAppResult(): AppResult<T> =
    fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(it.toDomainError()) }
    )

fun Throwable.toDomainError(): DomainError {
    val msg = message.orEmpty()

    // Conservative mapping; can be improved once backend error codes are known.
    if (msg.contains("unauthorized", ignoreCase = true)) return DomainError.Unauthorized
    if (msg.contains("forbidden", ignoreCase = true)) return DomainError.Forbidden
    if (msg.contains("not found", ignoreCase = true)) return DomainError.NotFound
    if (msg.contains("validation", ignoreCase = true)) return DomainError.Validation(message)
    if (msg.contains("http", ignoreCase = true) || msg.contains("network", ignoreCase = true)) return DomainError.Network(message)

    return DomainError.Unknown(message)
}

