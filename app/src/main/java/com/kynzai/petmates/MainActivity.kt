package com.kynzai.petmates

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import android.content.Intent
import com.kynzai.petmates.auth.OAuthLauncher
import com.kynzai.petmates.navigation.AppNavigation
import com.kynzai.petmates.ui.theme.PetMatesTheme
import com.kynzai.petmates.session.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var oauthLauncher: OAuthLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            sessionManager.restoreSession()
            handleOAuthIntent(intent)

            setContent {
                PetMatesTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(
                            sessionManager = sessionManager,
                            oauthLauncher = oauthLauncher,
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "com.kynzai.petmates" && uri.host == "auth-callback") {
            oauthLauncher.handleCallback(uri)
            intent.data = null
        }
    }
}
