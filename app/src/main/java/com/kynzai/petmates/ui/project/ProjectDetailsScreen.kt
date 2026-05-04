package com.kynzai.petmates.ui.project

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kynzai.petmates.ui.common.ScreenState
import com.kynzai.petmates.ui.common.EmptyStateScreen
import com.kynzai.petmates.ui.common.ErrorStateScreen
import com.kynzai.petmates.ui.common.LoadingStateScreen
import com.kynzai.petmates.ui.common.ServerUnavailableScreen
import com.kynzai.petmates.ui.common.UiEvent
import com.kynzai.petmates.ui.common.isServerUnavailableMessage
import com.kynzai.petmates.ui.mappers.VacancyUi
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    projectId: String? = null,
    onBackClick: () -> Unit = {},
    isAuthorized: Boolean = true,
    onAuthRequested: () -> Unit = {},
    onCreateVacancyClick: (String) -> Unit = {},
    onInviteUserClick: (String) -> Unit = {},
    onEditProjectClick: (String) -> Unit = {},
    onVacancyClick: (String) -> Unit = {},
    vm: ProjectDetailsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(projectId) { vm.load(projectId) }
    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                UiEvent.AuthRequired -> onAuthRequested()
                is UiEvent.ShowMessage -> Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                else -> Unit
            }
        }
    }

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
    ) { padding ->
        when (val s = state) {
            ScreenState.Loading -> LoadingStateScreen(message = "Загружаем проект...")

            ScreenState.Unauthorized -> Unit
            is ScreenState.Error -> {
                val modifier = Modifier.padding(padding)
                if (s.message.isServerUnavailableMessage()) {
                    ServerUnavailableScreen(modifier = modifier, onRetryClick = { vm.load(projectId) })
                } else {
                    ErrorStateScreen(modifier = modifier, message = s.message, onRetryClick = { vm.load(projectId) })
                }
            }
            is ScreenState.Content -> ProjectDetailsContent(
                data = s.value,
                modifier = Modifier.padding(padding),
                isAuthorized = isAuthorized,
                onRate = vm::rateProject,
                onAuthRequested = onAuthRequested,
                onEditProject = { onEditProjectClick(s.value.project.id) },
                onCreateVacancy = { onCreateVacancyClick(s.value.project.id) },
                onInvite = { onInviteUserClick(s.value.project.id) },
                onVacancyClick = onVacancyClick,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectDetailsContent(
    data: ProjectDetailsUiState,
    modifier: Modifier,
    isAuthorized: Boolean,
    onRate: (Int, String?) -> Unit,
    onAuthRequested: () -> Unit,
    onEditProject: () -> Unit,
    onCreateVacancy: () -> Unit,
    onInvite: () -> Unit,
    onVacancyClick: (String) -> Unit,
) {
    var showRatingDialog by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = PetMatesSurface)) {
                Column(Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(data.project.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PetMatesTextPrimary, modifier = Modifier.weight(1f))
                        Text(data.project.statusLabel, color = data.project.statusColor, fontWeight = FontWeight.Bold)
                    }
                    Text(data.project.ratingText, color = PetMatesTextSecondary, modifier = Modifier.padding(top = 4.dp))
                    Text(data.project.fullDescription.ifBlank { data.project.shortDescription }, color = PetMatesTextPrimary, modifier = Modifier.padding(top = 12.dp))
                }
            }
        }

        item {
            Text("Автор", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PetMatesTextPrimary)
            Text("@${data.owner.nickname} • ${data.owner.profileRole}", color = PetMatesTextSecondary, modifier = Modifier.padding(top = 4.dp))
        }

        item {
            Text("Участники", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PetMatesTextPrimary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                data.members.forEach { member -> AssistChip(onClick = {}, label = { Text(member.role) }) }
            }
        }

        item {
            if (data.isOwner) {
                OwnerActions(onEditProject, onCreateVacancy, onInvite)
            } else {
                Button(
                    onClick = { if (isAuthorized) showRatingDialog = true else onAuthRequested() },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("Оценить проект", fontWeight = FontWeight.Bold) }
            }
        }

        item {
            RatingsBlock(
                ratings = data.ratings,
                averageRating = data.averageRating,
            )
        }

        item { Text("Вакансии", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PetMatesTextPrimary) }
        if (data.vacancies.isEmpty()) {
            item {
                EmptyStateScreen(
                    title = "Нет вакансий",
                    message = "Владелец проекта пока не добавил открытые роли.",
                    modifier = Modifier.height(260.dp),
                )
            }
        } else {
            items(data.vacancies, key = { it.id }) { vacancy ->
                VacancyCard(vacancy = vacancy, onClick = { if (isAuthorized) onVacancyClick(vacancy.id) else onAuthRequested() })
            }
        }
    }

    if (showRatingDialog) {
        RatingDialog(
            onDismiss = { showRatingDialog = false },
            onSubmit = { score, comment ->
                showRatingDialog = false
                onRate(score, comment)
            },
        )
    }
}

@Composable
private fun RatingsBlock(
    ratings: List<ProjectRatingUi>,
    averageRating: Double?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PetMatesSurface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Оценки проекта", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PetMatesTextPrimary)
            Text(
                text = if (ratings.isEmpty()) {
                    "Оценок пока нет"
                } else {
                    "Средняя %.1f • всего ${ratings.size}".format(averageRating ?: 0.0)
                },
                color = PetMatesTextSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
            ratings.take(5).forEach { rating ->
                Column(Modifier.padding(top = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300))
                        Text("${rating.score}/5", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary)
                        Text(" • @${rating.userName}", color = PetMatesTextSecondary)
                    }
                    if (rating.comment.isNotBlank()) {
                        Text(rating.comment, color = PetMatesTextPrimary, modifier = Modifier.padding(top = 4.dp))
                    }
                    if (rating.date.isNotBlank()) {
                        Text(rating.date, color = PetMatesTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, String?) -> Unit,
) {
    var score by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Оценить проект", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { value ->
                        OutlinedButton(
                            onClick = { score = value },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (score == value) Color.White else PetMatesPrimary,
                                containerColor = if (score == value) PetMatesPrimary else Color.Transparent,
                            )
                        ) {
                            Text(value.toString())
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    label = { Text("Комментарий (опционально)") },
                    minLines = 3,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(score, comment.ifBlank { null }) }) {
                Text("Поставить оценку")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}

@Composable
private fun OwnerActions(onEdit: () -> Unit, onCreateVacancy: () -> Unit, onInvite: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onEdit, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White)) {
            Text("Редактировать проект", fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onCreateVacancy, modifier = Modifier.weight(1f)) { Text("Добавить вакансию") }
            OutlinedButton(onClick = onInvite, modifier = Modifier.weight(1f)) { Text("Пригласить") }
        }
    }
}

@Composable
private fun VacancyCard(vacancy: VacancyUi, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PetMatesSurface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(vacancy.title, fontWeight = FontWeight.Bold, color = PetMatesTextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(vacancy.description, color = PetMatesTextSecondary)
            Text(vacancy.statusLabel, color = if (vacancy.isOpen) PetMatesPrimary else PetMatesTextSecondary, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
