package com.kynzai.petmates.ui.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun ProjectDetailsScreen(
    projectId: String? = null,
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = PetMatesBackground,
        topBar = {
            TopAppBar(
                title = { Text("О проекте", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary) },
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
            if (projectId != null) {
                Text(
                    text = "ID проекта: $projectId",
                    fontSize = 12.sp,
                    color = PetMatesTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Заголовок и статус
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("PetMates Android App", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = PetMatesTextPrimary, modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PetMatesPrimary.copy(alpha = 0.12f)
                ) {
                    Text("В процессе", color = PetMatesPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Создатель: @kynzai", fontSize = 14.sp, color = PetMatesTextSecondary)
            Spacer(modifier = Modifier.height(16.dp))

            // Полное описание
            Text("Описание", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PetMatesTextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Приложение для поиска команды под пет-проекты. Мы строим платформу, которая поможет студентам и начинающим разработчикам находить единомышленников, получать первый опыт работы в команде и пополнять портфолио.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = PetMatesTextPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Открытые вакансии
            Text("Кого мы ищем (Вакансии)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PetMatesTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            // Карточка вакансии
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PetMatesSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("UI/UX Дизайнер", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PetMatesTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Нужно нарисовать логотип и доработать макеты мобильного приложения.", fontSize = 14.sp, color = PetMatesTextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { /* Откликнуться */ },
                        colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Откликнуться")
                    }
                }
            }
        }
    }
}
