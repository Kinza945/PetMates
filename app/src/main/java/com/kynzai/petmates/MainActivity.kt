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
import com.kynzai.petmates.navigation.AppNavigation
import com.kynzai.petmates.ui.theme.PetMatesTheme
import com.kynzai.petmates.session.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Делает приложение на весь экран (прозрачный статус-бар)

        lifecycleScope.launch {
            sessionManager.restoreSession()

            setContent {
                PetMatesTheme {
                    // Surface задает базовый фон для всего приложения
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Запускаем наш главный экран с навигацией!
                        AppNavigation(sessionManager = sessionManager)
                    }
                }
            }
        }
    }
}
