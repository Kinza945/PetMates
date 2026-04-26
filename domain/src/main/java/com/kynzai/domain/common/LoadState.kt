package com.kynzai.domain.common

/**
 * Унифицированное состояние загрузки для UI.
 * ViewModel может выставлять Loading перед вызовом use-case и затем конвертировать AppResult -> LoadState.
 */
sealed interface LoadState<out T> {
    data object Idle : LoadState<Nothing>
    data object Loading : LoadState<Nothing>
    data class Data<T>(val value: T) : LoadState<T>
    data class Error(val error: DomainError) : LoadState<Nothing>
}

fun <T> AppResult<T>.toLoadState(): LoadState<T> =
    when (this) {
        is AppResult.Success -> LoadState.Data(data)
        is AppResult.Error -> LoadState.Error(error)
    }

