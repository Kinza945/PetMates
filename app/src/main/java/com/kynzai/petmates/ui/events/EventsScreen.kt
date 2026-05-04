package com.kynzai.petmates.ui.events

import android.widget.Toast
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kynzai.domain.common.LoadState
import com.kynzai.petmates.ui.common.EmptyStateScreen
import com.kynzai.petmates.ui.common.ErrorStateScreen
import com.kynzai.petmates.ui.common.LoadingStateScreen
import com.kynzai.petmates.ui.common.ServerUnavailableScreen
import com.kynzai.petmates.ui.common.UiEvent
import com.kynzai.petmates.ui.common.isServerUnavailable
import com.kynzai.petmates.ui.common.toUiMessage
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary
import kotlinx.coroutines.delay
import java.util.UUID

private val TagBackground = Color(0xFFE0F2F1)
private val TagText = Color(0xFF216762)
private val OnlineGreen = Color(0xFF3AC83D)

@Composable
fun EventsRoute(
    onProjectClick: (String) -> Unit = {},
    onAuthRequested: () -> Unit = {},
    vm: EventsViewModel = hiltViewModel(),
) {
    var query by rememberSaveable { mutableStateOf("") }
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                UiEvent.AuthRequired -> onAuthRequested()
                is UiEvent.ShowMessage -> Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                else -> Unit
            }
        }
    }

    LaunchedEffect(query) {
        delay(250)
        vm.load(query)
    }

    EventsScreen(
        projects = (state as? LoadState.Data)?.value.orEmpty(),
        isLoading = state is LoadState.Loading,
        errorMessage = (state as? LoadState.Error)?.error?.toUiMessage(),
        isServerUnavailable = (state as? LoadState.Error)?.error?.isServerUnavailable() == true,
        query = query,
        onQueryChange = { query = it },
        onRetryClick = { vm.load(query) },
        onProjectClick = { id -> onProjectClick(id.toString()) },
        onRespondClick = { vacancyId -> vm.respondToVacancy(vacancyId) },
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    projects: List<FeedProjectUi>,
    isLoading: Boolean,
    errorMessage: String? = null,
    isServerUnavailable: Boolean = false,
    query: String,
    onQueryChange: (String) -> Unit,
    onRetryClick: () -> Unit = {},
    onProjectClick: (UUID) -> Unit = {},
    onRespondClick: (UUID) -> Unit = {},
) {
    val filtered = projects.filter {
        if (query.isBlank()) true
        else it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
    }
    var vacancyPickerProject by remember { mutableStateOf<FeedProjectUi?>(null) }

    vacancyPickerProject?.let { project ->
        ModalBottomSheet(onDismissRequest = { vacancyPickerProject = null }) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Выберите роль для отклика",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PetMatesTextPrimary,
                )
                Spacer(Modifier.height(12.dp))
                project.openVacancies.forEach { vacancy ->
                    Card(
                        onClick = {
                            vacancyPickerProject = null
                            onRespondClick(vacancy.vacancyId)
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = PetMatesSurface),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(vacancy.title, fontWeight = FontWeight.Bold, color = PetMatesTextPrimary)
                            Text(vacancy.tags.take(4).joinToString(" "), color = PetMatesTextSecondary, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
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
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("events_search"),
            placeholder = { Text("Поиск по названию", color = PetMatesTextSecondary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = PetMatesTextSecondary
                )
            },
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
            Spacer(modifier = Modifier.size(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = PetMatesTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        when {
            isLoading && filtered.isEmpty() -> LoadingStateScreen(message = "Загружаем мероприятия...")
            isServerUnavailable -> ServerUnavailableScreen(onRetryClick = onRetryClick)
            errorMessage != null -> ErrorStateScreen(message = errorMessage, onRetryClick = onRetryClick)
            filtered.isEmpty() -> EmptyStateScreen(
                title = if (query.isBlank()) "Нет проектов" else "Ничего не найдено",
                message = if (query.isBlank()) {
                    "Когда появятся проекты, они будут отображаться здесь."
                } else {
                    "Попробуйте изменить запрос поиска."
                },
            )
            else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
                    .testTag("events_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.projectId }) { item ->
                    ProjectCard(
                        item = item,
                        onClick = { onProjectClick(item.projectId) },
                        onRespondClick = {
                            when {
                                item.hasPendingResponse -> Unit
                                item.openVacancies.size > 1 -> vacancyPickerProject = item
                                item.openVacancies.size == 1 -> onRespondClick(item.openVacancies.first().vacancyId)
                                else -> Unit
                            }
                        },
                    )
                }
            }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectCard(
    item: FeedProjectUi,
    onClick: () -> Unit,
    onRespondClick: () -> Unit,
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
                        text = item.ratingSummary(),
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
                    onClick = onRespondClick,
                    enabled = item.openVacancies.isNotEmpty() && !item.hasPendingResponse,
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White)
                ) {
                    Text(
                        text = when {
                            item.hasPendingResponse -> "Отклик отправлен"
                            item.openVacancies.isEmpty() -> "Нет открытых ролей"
                            else -> "Откликнуться"
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

private fun FeedProjectUi.ratingSummary(): String =
    if (ratingCount <= 0) {
        "Оценок пока нет"
    } else {
        val avg = averageRating?.let { " • средняя %.1f".format(it) }.orEmpty()
        "$ratingCount оценок$avg"
    }
