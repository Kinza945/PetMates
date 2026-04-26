package com.kynzai.data.network

interface AuthTokenProvider {
    fun currentAccessToken(): String?
}

object EmptyAuthTokenProvider : AuthTokenProvider {
    override fun currentAccessToken(): String? = null
}

