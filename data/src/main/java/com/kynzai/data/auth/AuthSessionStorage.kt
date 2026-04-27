package com.kynzai.data.auth

import android.content.SharedPreferences
import android.util.Base64
import com.kynzai.domain.models.AuthSession
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

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
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .putLong(KEY_EXPIRES_AT, session.expiresAtEpochSeconds ?: 0L)
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
            refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null),
            expiresAtEpochSeconds = prefs.getLong(KEY_EXPIRES_AT, 0L).takeIf { it > 0L },
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
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_EXPIRES_AT = "expires_at"
    }
}

class EncryptedAuthSessionStorage(
    private val prefs: SharedPreferences,
) : AuthSessionStorage {
    /*
     * Минимальная защита токенов без дополнительных зависимостей:
     * значения шифруются AES/GCM ключом из Android Keystore, а в SharedPreferences
     * остаются только base64(iv + ciphertext). Для production можно заменить
     * реализацию на EncryptedSharedPreferences, не меняя AuthRepository.
     */
    override fun save(session: AuthSession) {
        prefs.edit()
            .putEncrypted(KEY_USER_ID, session.userId.toString())
            .putEncrypted(KEY_NICKNAME, session.nickname)
            .putEncrypted(KEY_EMAIL, session.email)
            .putEncrypted(KEY_ACCESS_TOKEN, session.accessToken)
            .putEncrypted(KEY_REFRESH_TOKEN, session.refreshToken)
            .putEncrypted(KEY_EXPIRES_AT, session.expiresAtEpochSeconds?.toString())
            .apply()
    }

    override fun load(): AuthSession? {
        val userId = prefs.getEncrypted(KEY_USER_ID)?.let { raw ->
            runCatching { UUID.fromString(raw) }.getOrNull()
        } ?: return null
        val nickname = prefs.getEncrypted(KEY_NICKNAME)?.takeIf { it.isNotBlank() } ?: return null

        return AuthSession(
            userId = userId,
            nickname = nickname,
            email = prefs.getEncrypted(KEY_EMAIL),
            accessToken = prefs.getEncrypted(KEY_ACCESS_TOKEN),
            refreshToken = prefs.getEncrypted(KEY_REFRESH_TOKEN),
            expiresAtEpochSeconds = prefs.getEncrypted(KEY_EXPIRES_AT)?.toLongOrNull(),
        )
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    private fun SharedPreferences.Editor.putEncrypted(key: String, value: String?): SharedPreferences.Editor =
        if (value == null) remove(key) else putString(key, encrypt(value))

    private fun SharedPreferences.getEncrypted(key: String): String? =
        getString(key, null)?.let { encrypted ->
            runCatching { decrypt(encrypted) }.getOrNull()
        }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val payload = cipher.iv + encrypted
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val payload = Base64.decode(value, Base64.NO_WRAP)
        val iv = payload.copyOfRange(0, GCM_IV_BYTES)
        val encrypted = payload.copyOfRange(GCM_IV_BYTES, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(encrypted).toString(Charsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KEY_ALGORITHM, ANDROID_KEYSTORE).run {
            init(
                android.security.keystore.KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                        android.security.keystore.KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
            generateKey()
        }
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "petmates_auth_session"
        const val KEY_ALGORITHM = "AES"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_IV_BYTES = 12
        const val GCM_TAG_BITS = 128
        const val KEY_USER_ID = "user_id"
        const val KEY_NICKNAME = "nickname"
        const val KEY_EMAIL = "email"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_EXPIRES_AT = "expires_at"
    }
}
