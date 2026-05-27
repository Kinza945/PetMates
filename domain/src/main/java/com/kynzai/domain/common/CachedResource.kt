package com.kynzai.domain.common

data class CachedResource<T>(
    val data: T? = null,
    val isRefreshing: Boolean = false,
    val error: Throwable? = null,
)

