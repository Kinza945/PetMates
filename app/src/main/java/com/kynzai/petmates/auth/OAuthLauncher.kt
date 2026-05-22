package com.kynzai.petmates.auth

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.kynzai.data.auth.OAuthPkce
import com.kynzai.domain.models.SocialAuthProvider
import com.kynzai.domain.repositories.AuthRepository
import com.kynzai.petmates.session.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OAuthLauncher @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var onFinished: ((Result<Unit>) -> Unit)? = null

    fun start(
        context: Context,
        provider: SocialAuthProvider,
        onFinished: (Result<Unit>) -> Unit,
    ) {
        val verifier = OAuthPkce.generateVerifier()
        val challenge = OAuthPkce.challenge(verifier)
        prefs.edit().putString(KEY_CODE_VERIFIER, verifier).apply()

        val url = authRepository.buildOAuthAuthorizeUrl(provider, challenge).getOrElse { error ->
            clearVerifier()
            onFinished(Result.failure(error))
            return
        }

        this.onFinished = onFinished

        if (url.startsWith("mock://")) {
            scope.launch {
                val result = sessionManager
                    .completeOAuthSignIn(url, verifier)
                    .map { Unit }
                clearVerifier()
                onFinished(result)
            }
            return
        }

        runCatching {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                .launchUrl(context, Uri.parse(url))
        }.onFailure { error ->
            clearVerifier()
            this.onFinished = null
            onFinished(Result.failure(error))
        }
    }

    fun handleCallback(uri: Uri?) {
        val callbackUri = uri?.toString() ?: return
        val callback = onFinished
        onFinished = null

        val verifier = prefs.getString(KEY_CODE_VERIFIER, null)
        if (verifier.isNullOrBlank()) {
            callback?.invoke(
                Result.failure(IllegalStateException("OAuth session expired. Try signing in again.")),
            )
            return
        }

        scope.launch {
            val result = sessionManager
                .completeOAuthSignIn(callbackUri, verifier)
                .map { Unit }
            clearVerifier()
            callback?.invoke(result)
        }
    }

    fun hasPendingFlow(): Boolean = onFinished != null

    private fun clearVerifier() {
        prefs.edit().remove(KEY_CODE_VERIFIER).apply()
    }

    private companion object {
        const val PREFS_NAME = "petmates_oauth"
        const val KEY_CODE_VERIFIER = "code_verifier"
    }
}
