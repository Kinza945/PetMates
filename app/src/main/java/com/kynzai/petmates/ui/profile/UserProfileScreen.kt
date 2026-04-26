package com.kynzai.petmates.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    nickname: String? = null,
    onBackClick: () -> Unit = {},
    isAuthorized: Boolean,          // ДОБАВИТЬ ЭТО
    onAuthRequested: () -> Unit,    // ДОБАВИТЬ ЭТО
    onInviteToProject: (String) -> Unit // ДОБАВИТЬ ЭТО (принимает ID проекта)
) {
    val context = LocalContext.current
    Scaffold(
        containerColor = PetMatesBackground,
        topBar = {
            TopAppBar(
                title = { Text("Профиль", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = PetMatesTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PetMatesSurface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Шапка профиля
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp)) }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(nickname ?: "DogI1X", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = PetMatesTextPrimary)
                    Text("Гринькин Вадим", fontSize = 16.sp, color = PetMatesTextSecondary)
                    Text("Россия, Краснодар", fontSize = 14.sp, color = PetMatesTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // О себе
            Text("О себе", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PetMatesTextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Добавлю немного описания, чтобы быть самым модным на районе. Буду рад, если смогу научиться чему-нибудь интересному.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = PetMatesTextPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Навыки
            Text("Навыки", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PetMatesTextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("C#") })
                AssistChip(onClick = {}, label = { Text("REST API") })
                AssistChip(onClick = {}, label = { Text("Git") })
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка действия (Если чужой профиль)
            Button(
                onClick = {
                    Toast.makeText(context, "Приглашение отправлено (мок)", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary)
            ) {
                Text("Пригласить в проект", fontSize = 16.sp)
            }
        }
    }
}
