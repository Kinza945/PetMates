package com.kynzai.petmates.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Фирменный цвет из макета (бирюзовый)
val PetMatesTeal = Color(0xFF38B29E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Заглушка для логотипа
                        Icon(Icons.Default.Pets, contentDescription = "Logo", modifier = Modifier.padding(end = 8.dp))
                        Text("PetMates", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Уведомления")
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Меню")
                    }
                }
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
            // Вкладки (Tabs)
            TabRow(
                selectedTabIndex = 0,
                containerColor = Color.Transparent,
                contentColor = PetMatesTeal,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Tab(selected = true, onClick = { }, text = { Text("Информация", color = PetMatesTeal) })
                Tab(selected = false, onClick = { }, text = { Text("Активность", color = Color.Gray) })
                Tab(selected = false, onClick = { }, text = { Text("Уведомления", color = Color.Gray) })
                Tab(selected = false, onClick = { }, text = { Text("Настройки", color = Color.Gray) })
            }

            // Аватар и Никнейм
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Avatar", tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Имя пользователя:", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = "DogI1X",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Основные поля формы
            FormRow(label = "Реальное имя пользователя:", value = "Гринькин Вадим Николаевич")
            FormRow(label = "Возраст:", value = "17")

            // Выбор пола (Radio Buttons)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Пол:", modifier = Modifier.weight(0.35f), fontSize = 14.sp)
                Row(modifier = Modifier.weight(0.65f), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = true, onClick = {}, colors = RadioButtonDefaults.colors(selectedColor = PetMatesTeal))
                    Text("Мужской", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = false, onClick = {})
                    Text("Женский", fontSize = 14.sp)
                }
            }

            FormRow(label = "Страна:", value = "Россия")
            FormRow(label = "Город:", value = "Краснодар")
            FormRow(label = "Место работы/учебы:", value = "ИМСИТ")
            FormRow(label = "Роль:", value = "Python Data Science")

            Spacer(modifier = Modifier.height(16.dp))

            // Текстовые области (Описание, Скиллы)
            SectionTitle(icon = Icons.Default.Edit, title = "Описание:")
            MultilineTextField("Добавлю немного описания, чтобы быть самым модным на районе. Буду рад, если смогу научиться чему-нибудь интересному.\n\nДля связи:\nТГ: @ClownZzz")

            SectionTitle(icon = Icons.Default.Bookmark, title = "hard-skills: (перечисление тегами)")
            MultilineTextField("#csharp #rest #patterns #ui/ux #git #design #databases")

            SectionTitle(icon = Icons.Default.Star, title = "soft-skills: (перечисление тегами)")
            MultilineTextField("#величайший #прекраснейший #сильнейший")

            // Контакты
            SectionTitle(icon = Icons.Default.Link, title = "Контакты:")
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ютуб-канал:", fontSize = 14.sp)
                Text("https://www.youtube.com/@spektr.project", color = PetMatesTeal, fontSize = 14.sp)
                Row {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PetMatesTeal, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                }
            }

            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добавить контакт", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Нижние кнопки сохранения
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = PetMatesTeal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сохранить", color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена", color = Color.Black)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Переключатель предпросмотра
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Предпросмотр", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = false,
                        onCheckedChange = {},
                        colors = SwitchDefaults.colors(checkedThumbColor = PetMatesTeal, checkedTrackColor = PetMatesTeal.copy(alpha = 0.5f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Вспомогательная функция для отрисовки строки "Лейбл + Поле ввода"
@Composable
fun FormRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(0.35f), fontSize = 14.sp)
        OutlinedTextField(
            value = value,
            onValueChange = {}, // Пусто, так как это заглушка
            modifier = Modifier.weight(0.65f).height(50.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}

// Вспомогательная функция для заголовков секций
@Composable
fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

// Вспомогательная функция для многострочных полей
@Composable
fun MultilineTextField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 100.dp),
        shape = RoundedCornerShape(8.dp)
    )
}