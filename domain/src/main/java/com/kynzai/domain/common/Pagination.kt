package com.kynzai.domain.common

sealed interface PageToken {
    data class Offset(val value: Int) : PageToken
    data class Cursor(val value: String) : PageToken
}

data class PageRequest(
    val limit: Int = 20,
    val token: PageToken? = null,
)

data class Page<T>(
    val items: List<T>,
    val nextToken: PageToken? = null,
)

