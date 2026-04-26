package com.kynzai.petmates.ui.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.kynzai.petmates.ui.profile.NotificationsRoute
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    isAuthorized: Boolean,
    onBackClick: () -> Unit,
    onAuthRequested: () -> Unit,
) {
    Scaffold(
        containerColor = PetMatesBackground,
        topBar = {
            TopAppBar(
                title = { Text("Уведомления", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = PetMatesTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PetMatesSurface)
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = PetMatesBackground,
        ) {
            if (!isAuthorized) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onAuthRequested,
                        colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White)
                    ) {
                        Text("Авторизоваться", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                NotificationsRoute(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    showHeader = false,
                )
            }
        }
    }
}
