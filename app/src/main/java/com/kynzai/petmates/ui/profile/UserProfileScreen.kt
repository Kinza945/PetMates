package com.kynzai.petmates.ui.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kynzai.domain.models.User
import com.kynzai.petmates.ui.common.ScreenState
import com.kynzai.petmates.ui.common.RemoteAvatar
import com.kynzai.petmates.ui.common.UiEvent
import com.kynzai.petmates.ui.mappers.ProjectUi
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
    isAuthorized: Boolean,
    onAuthRequested: () -> Unit,
    onInviteToProject: (String) -> Unit,
    vm: UserProfileViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var showProjectPicker by remember { mutableStateOf(false) }

    LaunchedEffect(nickname) { vm.load(nickname) }
    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                UiEvent.AuthRequired -> onAuthRequested()
                is UiEvent.NavigateToInvite -> onInviteToProject(event.projectId)
                is UiEvent.ShowMessage -> Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                else -> Unit
            }
        }
    }

    Scaffold(
        containerColor = PetMatesBackground,
        topBar = {
            TopAppBar(
                title = { Text("Профиль", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = PetMatesTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PetMatesSurface)
            )
        }
    ) { padding ->
        when (val s = state) {
            ScreenState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PetMatesPrimary)
            }
            ScreenState.Unauthorized -> Unit
            is ScreenState.Error -> Text(s.message, color = PetMatesTextSecondary, modifier = Modifier.padding(padding).padding(16.dp))
            is ScreenState.Content -> {
                UserProfileContent(
                    data = s.value.user.toDomainModel(),
                    isAuthorized = isAuthorized,
                    modifier = Modifier.padding(padding),
                    onInviteClick = {
                        if (!s.value.isAuthorized) onAuthRequested()
                        else if (s.value.myProjects.size > 1) showProjectPicker = true
                        else vm.onInviteClick()
                    }
                )
                if (showProjectPicker) {
                    ProjectPickerSheet(
                        projects = s.value.myProjects,
                        onDismiss = { showProjectPicker = false },
                        onSelected = {
                            showProjectPicker = false
                            onInviteToProject(it.id)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserProfileContent(
    data: User,
    isAuthorized: Boolean,
    modifier: Modifier,
    onInviteClick: () -> Unit,
) {
    val locationChips = listOfNotNull(
        data.formattedCountry(),
        data.formattedCity(),
        data.formattedWorkplace(),
    )
    val profileSummary = listOfNotNull(
        data.formattedAge(),
        data.formattedGender(),
        data.formattedCity(),
        data.formattedWorkplace(),
    ).joinToString(" • ")
    val contacts = data.profileContacts()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RemoteAvatar(
                    avatarUrl = data.avatarUrl,
                    size = 100.dp,
                )

                Text(
                    text = data.displayName(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = PetMatesTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Text(
                    text = data.email?.takeIf { it.isNotBlank() } ?: "Почта не указана",
                    color = PetMatesTextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = data.formattedStatus(),
                    color = PetMatesPrimary,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )

                if (locationChips.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        locationChips.forEach { OutlinedUserInfoChip(it) }
                    }
                }

                if (profileSummary.isNotBlank()) {
                    Text(
                        text = profileSummary,
                        color = PetMatesTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        }
        item {
            UserSectionTitle("Основное:")
            UserDetailsCard(user = data)
        }
        item {
            UserSectionTitle("Описание:")
            Text(
                text = data.description?.takeIf { it.isNotBlank() } ?: "Описание не указано.",
                color = PetMatesTextPrimary,
                fontSize = 14.sp,
            )
        }
        item {
            UserSectionTitle("Для связи:")
            if (contacts.isEmpty()) {
                Text(text = "Контакты не указаны.", color = PetMatesTextSecondary, fontSize = 14.sp)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    contacts.forEach { (name, link) ->
                        Text(
                            text = "$name: $link",
                            color = PetMatesTextPrimary,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        item {
            UserSectionTitle("hard-skills:")
            UserSkillRow(skills = data.hardSkills)
        }
        item {
            UserSectionTitle("soft-skills:")
            UserSkillRow(skills = data.softSkills)
        }
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onInviteClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White),
            ) {
                Text(
                    if (isAuthorized) "Пригласить в проект" else "Авторизоваться для приглашения",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun OutlinedUserInfoChip(text: String) {
    AssistChip(
        onClick = { },
        label = { Text(text = text, color = PetMatesTextSecondary, fontSize = 12.sp) },
        shape = RoundedCornerShape(percent = 50),
        border = BorderStroke(1.dp, PetMatesTextSecondary.copy(alpha = 0.65f)),
        colors = AssistChipDefaults.assistChipColors(containerColor = Color.Transparent),
    )
}

@Composable
private fun UserDetailsCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PetMatesSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            UserDetailRow(label = "Страна", value = user.formattedCountry())
            UserDetailRow(label = "Город", value = user.formattedCity())
            UserDetailRow(label = "Учёба / работа", value = user.formattedWorkplace())
            UserDetailRow(label = "Возраст", value = user.formattedAge())
            UserDetailRow(label = "Пол", value = user.formattedGender())
            UserDetailRow(label = "Статус", value = user.status)
        }
    }
}

@Composable
private fun UserDetailRow(label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            color = PetMatesTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.weight(0.42f),
        )
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "Не указано",
            color = PetMatesTextPrimary,
            fontSize = 14.sp,
            modifier = Modifier.weight(0.58f),
        )
    }
}

@Composable
private fun UserSectionTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = PetMatesTextPrimary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserSkillRow(skills: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (skills.isEmpty()) {
            Text(text = "Не указаны.", color = PetMatesTextSecondary, fontSize = 14.sp)
        } else {
            skills.forEach { skill ->
                AssistChip(
                    onClick = { },
                    label = { Text(skill, color = PetMatesPrimary, fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectPickerSheet(
    projects: List<ProjectUi>,
    onDismiss: () -> Unit,
    onSelected: (ProjectUi) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            item {
                Text("Выберите проект", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PetMatesTextPrimary)
            }
            items(projects, key = { it.id }) { project ->
                TextButton(onClick = { onSelected(project) }, modifier = Modifier.fillMaxWidth()) {
                    Text(project.name, color = PetMatesPrimary)
                }
            }
        }
    }
}

private fun com.kynzai.petmates.ui.mappers.UserUi.toDomainModel(): com.kynzai.domain.models.User {
    // Безопасно извлекаем аватарку через рефлексию или прямое свойство, если оно доступно
    val parsedAvatar = runCatching {
        val field = this::class.java.declaredFields.firstOrNull { it.name.contains("avatar", ignoreCase = true) }
        field?.isAccessible = true
        field?.get(this) as? String
    }.getOrNull() ?: ""

    return com.kynzai.domain.models.User(
        userId = java.util.UUID.fromString(this.id),
        nickname = this.nickname.takeIf { it.isNotBlank() } ?: "user",
        username = this.nickname,
        email = null,
        avatarUrl = parsedAvatar,
        age = this.age.toIntOrNull(),
        gender = com.kynzai.domain.models.Gender.UNSPECIFIED,
        country = this.country,
        city = this.city,
        workplace = this.workplace,
        status = this.profileRole,
        profileRole = this.profileRole,
        description = this.description,
        hardSkills = this.hardSkills,
        softSkills = this.softSkills,
        contacts = emptyList()
    )
}