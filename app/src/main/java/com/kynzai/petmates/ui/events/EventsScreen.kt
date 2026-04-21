package com.kynzai.petmates.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary

private val TagBackground = Color(0xFFE0F2F1)
private val TagText = Color(0xFF216762)
private val OnlineGreen = Color(0xFF3AC83D)

private data class FeedProjectUi(
    val id: String,
    val role: String,
    val ratingCount: Int,
    val name: String,
    val description: String,
    val tags: List<String>,
    val authorName: String,
    val isOnline: Boolean,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventsScreen(
    onProjectClick: (String) -> Unit = {},
) {
    var query by remember { mutableStateOf("") }

    val projects = remember {
        listOf(
            FeedProjectUi(
                id = "p1",
                role = "Frontend-разработчик",
                ratingCount = 4,
                name = "Приложение Contacts",
                description = "Нашему проекту требуется разработчик, который сможет сверстать сайт на React. Нужно будет собрать страницу, формы и базовые компоненты интерфейса.",
                tags = listOf("#web", "#react", "#hooks"),
                authorName = "Clown[???]",
                isOnline = true,
            ),
            FeedProjectUi(
                id = "p2",
                role = "Android-разработчик",
                ratingCount = 12,
                name = "PetMates Mobile",
                description = "Ищем Android разработчика для работы с Jetpack Compose, Ktor client и Supabase. Нужны аккуратные UI и чистая архитектура.",
                tags = listOf("#kotlin", "#compose", "#ktor"),
                authorName = "kynzai",
                isOnline = false,
            ),
            FeedProjectUi(
                id = "p3",
                role = "Backend-разработчик",
                ratingCount = 7,
                name = "Task Tracker API",
                description = "Нужно поднять REST API на Ktor, подключить PostgreSQL, настроить миграции и базовую авторизацию. Важно: чистые слои и тесты.",
                tags = listOf("#ktor", "#postgres", "#clean"),
                authorName = "BackendEnjoyer",
                isOnline = true,
            ),
            FeedProjectUi(
                id = "p4",
                role = "UI/UX дизайнер",
                ratingCount = 2,
                name = "Mobile Design System",
                description = "Собираем библиотеку компонентов для мобильного приложения. Нужны экраны, типографика, токены и документация.",
                tags = listOf("#figma", "#uiux", "#design"),
                authorName = "DesignMaster",
                isOnline = true,
            ),
            FeedProjectUi(
                id = "p5",
                role = "QA инженер",
                ratingCount = 9,
                name = "PetMates QA",
                description = "Настроить набор автотестов, базовый тест-план и регрессию. Плюс — опыт с Compose UI test и mock-сервером.",
                tags = listOf("#qa", "#testing", "#compose"),
                authorName = "qa_cat",
                isOnline = false,
            ),
            FeedProjectUi(
                id = "p6",
                role = "DevOps",
                ratingCount = 5,
                name = "CI для PetMates",
                description = "Нужен GitHub Actions: сборка, линт, тесты, артефакты, релизные сборки. По желанию — docker для backend.",
                tags = listOf("#ci", "#github", "#devops"),
                authorName = "ops_guy",
                isOnline = false,
            ),
            FeedProjectUi(
                id = "p7",
                role = "Data Analyst",
                ratingCount = 1,
                name = "Analytics Dashboard",
                description = "Сделать прототип аналитики: события, конверсии, retention. Нужна схема данных и валидация качества.",
                tags = listOf("#sql", "#analytics", "#dashboard"),
                authorName = "data_fox",
                isOnline = true,
            ),
            FeedProjectUi(
                id = "p8",
                role = "iOS разработчик",
                ratingCount = 3,
                name = "PetMates iOS",
                description = "Делаем iOS клиент для PetMates. Нужны базовые экраны, сетевой слой и архитектура.",
                tags = listOf("#ios", "#swift", "#mobile"),
                authorName = "swiftie",
                isOnline = false,
            ),
            FeedProjectUi(
                id = "p9",
                role = "Product Manager",
                ratingCount = 6,
                name = "PetMates Roadmap",
                description = "Собрать роадмап, разметить приоритеты, собрать фидбек от команды. Нужно вести бэклог и релизы.",
                tags = listOf("#pm", "#roadmap", "#planning"),
                authorName = "product_ninja",
                isOnline = true,
            ),
            FeedProjectUi(
                id = "p10",
                role = "Frontend-разработчик",
                ratingCount = 11,
                name = "Landing PetMates",
                description = "Лендинг с описанием платформы, формой заявки и CTA. Нужны анимации и адаптив.",
                tags = listOf("#frontend", "#landing", "#css"),
                authorName = "web_tiger",
                isOnline = true,
            ),
        )
    }

    val filtered = projects.filter { p ->
        query.isBlank() || p.name.contains(query, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PetMatesBackground)
            .padding(16.dp)
            .testTag("events_screen")
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("events_search"),
            placeholder = { Text("Поиск по названию", color = PetMatesTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PetMatesTextSecondary) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PetMatesPrimary,
                cursorColor = PetMatesPrimary,
                unfocusedBorderColor = PetMatesTextSecondary.copy(alpha = 0.5f),
                focusedTextColor = PetMatesTextPrimary,
                unfocusedTextColor = PetMatesTextPrimary,
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Сортировка по дате добавления",
                fontSize = 14.sp,
                color = PetMatesTextSecondary
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = PetMatesTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
                .testTag("events_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered, key = { it.id }) { item ->
                ProjectCard(item = item, onClick = { onProjectClick(item.id) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectCard(
    item: FeedProjectUi,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PetMatesSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.role,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PetMatesTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PetMatesTextSecondary
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "${item.ratingCount} оценок",
                        fontSize = 12.sp,
                        color = PetMatesTextSecondary
                    )
                }
            }

            Text(
                text = item.name,
                color = PetMatesPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = item.description,
                fontSize = 14.sp,
                color = PetMatesTextPrimary,
                modifier = Modifier.padding(top = 8.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            FlowRow(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item.tags.take(3).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(TagBackground, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = tag, color = TagText, fontSize = 12.sp)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.LightGray, CircleShape)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Column {
                        Text(
                            text = item.authorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PetMatesTextPrimary
                        )
                        Text(
                            text = if (item.isOnline) "• онлайн" else "• офлайн",
                            fontSize = 12.sp,
                            color = if (item.isOnline) OnlineGreen else PetMatesTextSecondary
                        )
                    }
                }

                Button(
                    onClick = { /* TODO: respond */ },
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White)
                ) {
                    Text(text = "Откликнуться", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
