package com.kynzai.data.auth

import android.content.SharedPreferences
import com.kynzai.domain.models.AuthSession
import java.util.UUID

interface AuthSessionStorage {
    fun save(session: AuthSession)
    fun load(): AuthSession?
    fun clear()
}

class SharedPreferencesAuthSessionStorage(
    private val prefs: SharedPreferences,
) : AuthSessionStorage {
    override fun save(session: AuthSession) {
        prefs.edit()
            .putString(KEY_USER_ID, session.userId.toString())
            .putString(KEY_NICKNAME, session.nickname)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_ACCESS_TOKEN, session.accessToken)
            .apply()
    }

    override fun load(): AuthSession? {
        val userId = prefs.getString(KEY_USER_ID, null)?.let { raw ->
            runCatching { UUID.fromString(raw) }.getOrNull()
        } ?: return null

        val nickname = prefs.getString(KEY_NICKNAME, null)?.takeIf { it.isNotBlank() } ?: return null

        return AuthSession(
            userId = userId,
            nickname = nickname,
            email = prefs.getString(KEY_EMAIL, null),
            accessToken = prefs.getString(KEY_ACCESS_TOKEN, null),
        )
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_NICKNAME = "nickname"
        const val KEY_EMAIL = "email"
        const val KEY_ACCESS_TOKEN = "access_token"
    }
}
