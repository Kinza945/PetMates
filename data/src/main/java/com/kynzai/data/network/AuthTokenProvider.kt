package com.kynzai.data.network

import com.kynzai.data.auth.AuthSessionStorage

interface AuthTokenProvider {
    fun currentAccessToken(): String?
}

object EmptyAuthTokenProvider : AuthTokenProvider {
    override fun currentAccessToken(): String? = null
}

class StorageAuthTokenProvider(
    private val storage: AuthSessionStorage,
) : AuthTokenProvider {
    override fun currentAccessToken(): String? = storage.load()?.accessToken
}

