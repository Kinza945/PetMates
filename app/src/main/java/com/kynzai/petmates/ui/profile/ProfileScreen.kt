package com.kynzai.petmates.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kynzai.petmates.ui.common.AuthRequiredScreen
import com.kynzai.petmates.ui.common.ErrorStateScreen
import com.kynzai.petmates.ui.common.LoadingStateScreen
import com.kynzai.petmates.ui.common.ScreenState
import com.kynzai.petmates.ui.common.ServerUnavailableScreen
import com.kynzai.petmates.ui.common.UiEvent
import com.kynzai.petmates.ui.common.isServerUnavailableMessage
import com.kynzai.petmates.ui.mappers.ContactUi
import com.kynzai.petmates.ui.mappers.ProjectUi
import com.kynzai.petmates.ui.mappers.UserUi
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary

private val TagText = Color(0xFF216762)
private val OnlineGreen = Color(0xFF3AC83D)
private val Danger = Color(0xFFE53935)
private val ErrorContainer = Color(0xFFFFEBEE)

private enum class ProfileTab { Info, Activity, Notifications, Settings }

@Composable
fun ProfileScreen(
    onCreateProjectClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onLogoutComplete: () -> Unit = {},
    onAuthRequested: () -> Unit = {},
    onOpenProject: (String) -> Unit = {},
    onEditProject: (String) -> Unit = {},
    onCreateVacancy: (String) -> Unit = {},
    onOpenVacancy: (String) -> Unit = {},
    vm: ProfileViewModel = hiltViewModel(),
) {
    val scrollState = rememberScrollState()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var selectedTab by rememberSaveable { mutableIntStateOf(ProfileTab.Info.ordinal) }
    val safeTabIndex = selectedTab.coerceIn(0, ProfileTab.entries.lastIndex)

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            if (event is UiEvent.ShowMessage) {
                Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (val s = state) {
        ScreenState.Loading -> {
            LoadingStateScreen(message = "Загружаем профиль...")
            return
        }

        ScreenState.Unauthorized -> {
            AuthRequiredScreen(
                message = "Войдите в аккаунт, чтобы открыть профиль и ваши проекты.",
                onAuthClick = onAuthRequested,
            )
            return
        }

        is ScreenState.Error -> {
            if (s.message.isServerUnavailableMessage()) {
                ServerUnavailableScreen(onRetryClick = vm::refresh)
            } else {
                ErrorStateScreen(message = s.message, onRetryClick = vm::refresh)
            }
            return
        }

        is ScreenState.Content -> {
            val profile = s.value
            val user = profile.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PetMatesSurface)
            .verticalScroll(rememberScrollState())
    ) {
        // Шапка профиля: теперь данные приходят из ProfileViewModel, а не из hardcoded макета.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.LightGray, CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user.nickname,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PetMatesTextPrimary
            )
            Text(
                text = user.realName.ifBlank { "Имя не указано" },
                fontSize = 14.sp,
                color = PetMatesTextSecondary
            )
            Text(
                text = user.profileRole.ifBlank { "Роль не указана" },
                fontSize = 16.sp,
                color = PetMatesPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )

            FlowRow(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(user.country, user.city, user.workplace)
                    .filter { it.isNotBlank() }
                    .forEach { OutlinedInfoChip(it) }
            }

            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "• онлайн", color = OnlineGreen, fontSize = 12.sp)
                Text(
                    text = listOf(user.age, user.city, user.workplace).filter { it.isNotBlank() }.joinToString(" • ", prefix = "  "),
                    color = PetMatesTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onCreateProjectClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White)
            ) {
                Text(text = "Создать проект", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onEditProfileClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PetMatesPrimary)
            ) {
                Text(text = "Редактировать", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ScrollableTabRow(
            selectedTabIndex = safeTabIndex,
            containerColor = PetMatesSurface,
            contentColor = PetMatesPrimary,
            edgePadding = 16.dp,
        ) {
            Tab(
                selected = selectedTab == ProfileTab.Info.ordinal,
                onClick = { selectedTab = ProfileTab.Info.ordinal },
                text = { Text("Информация") },
                selectedContentColor = PetMatesPrimary,
                unselectedContentColor = PetMatesTextSecondary,
            )
            Tab(
                selected = selectedTab == ProfileTab.Activity.ordinal,
                onClick = { selectedTab = ProfileTab.Activity.ordinal },
                text = { Text("Активность") },
                selectedContentColor = PetMatesPrimary,
                unselectedContentColor = PetMatesTextSecondary,
            )
            Tab(
                selected = selectedTab == ProfileTab.Notifications.ordinal,
                onClick = { selectedTab = ProfileTab.Notifications.ordinal },
                text = { Text("Уведомления") },
                selectedContentColor = PetMatesPrimary,
                unselectedContentColor = PetMatesTextSecondary,
            )
            Tab(
                selected = selectedTab == ProfileTab.Settings.ordinal,
                onClick = { selectedTab = ProfileTab.Settings.ordinal },
                text = { Text("Настройки") },
                selectedContentColor = PetMatesPrimary,
                unselectedContentColor = PetMatesTextSecondary,
            )
        }

        // Контент вкладок должен иметь ограниченную высоту, иначе Compose падает на вложенных scroll-контейнерах.
        Box(modifier = Modifier.fillMaxWidth()) {
            when (ProfileTab.entries.getOrNull(selectedTab) ?: ProfileTab.Info) {
                ProfileTab.Info -> InfoTab(
                    modifier = Modifier.fillMaxSize(),
                    description = user.description,
                    contacts = user.contacts,
                    hardSkills = user.hardSkills,
                    softSkills = user.softSkills,
                )

                ProfileTab.Activity -> ActivityTab(
                    modifier = Modifier.fillMaxSize(),
                    myProjects = profile.myProjects,
                    myResponses = profile.myResponses,
                    myInvites = profile.myInvites,
                    sentInvites = profile.sentInvites,
                    onOpenProject = onOpenProject,
                    onEditProject = onEditProject,
                    onCreateVacancy = onCreateVacancy,
                    onOpenVacancy = onOpenVacancy,
                    onCancelResponse = vm::cancelResponse,
                    onAcceptInvite = vm::acceptInvite,
                    onDeclineInvite = vm::declineInvite,
                    onCancelSentInvite = vm::cancelSentInvite,
                )

                ProfileTab.Notifications -> NotificationsRoute(modifier = Modifier.fillMaxSize())
                ProfileTab.Settings -> SettingsTab(
                    accountName = user.nickname,
                    modifier = Modifier.fillMaxSize(),
                    onEditProfileClick = onEditProfileClick,
                    onLogoutComplete = onLogoutComplete,
                )
            }
        }
    }
        }
    }
}

@Composable
private fun OutlinedInfoChip(text: String) {
    AssistChip(
        onClick = { /* no-op */ },
        label = { Text(text = text, color = PetMatesTextSecondary, fontSize = 12.sp) },
        shape = RoundedCornerShape(percent = 50),
        border = BorderStroke(1.dp, PetMatesTextSecondary.copy(alpha = 0.65f)),
        colors = AssistChipDefaults.assistChipColors(containerColor = Color.Transparent),
    )
}

@Composable
private fun PlaceholderTab(
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Text(text = "$title (в разработке)", color = PetMatesTextSecondary)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoTab(
    description: String,
    contacts: List<ContactUi>,
    hardSkills: List<String>,
    softSkills: List<String>,
    modifier: Modifier = Modifier,
) {
    val tagBg = PetMatesPrimary.copy(alpha = 0.25f)

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle("Описание:")
        Text(text = description, color = PetMatesTextPrimary, fontSize = 14.sp)

        SectionTitle("Для связи:")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            contacts.forEach { contact ->
                Text(
                    text = "${contact.name}: ${contact.link}",
                    color = PetMatesTextPrimary,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        SectionTitle("hard-skills:")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            hardSkills.forEach { Chip(text = it, background = tagBg) }
        }

        SectionTitle("soft-skills:")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            softSkills.forEach { Chip(text = it, background = tagBg) }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ActivityTab(
    myProjects: List<ProjectUi>,
    myResponses: List<ProfileResponseUi>,
    myInvites: List<ProfileInviteUi>,
    sentInvites: List<ProfileSentInviteUi>,
    onOpenProject: (String) -> Unit,
    onEditProject: (String) -> Unit,
    onCreateVacancy: (String) -> Unit,
    onOpenVacancy: (String) -> Unit,
    onCancelResponse: (String) -> Unit,
    onAcceptInvite: (String) -> Unit,
    onDeclineInvite: (String) -> Unit,
    onCancelSentInvite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // ВАЖНО: LazyColumn заменен на Column
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionTitle("Мои проекты")
        if (myProjects.isEmpty()) {
            EmptyActivityText("У вас пока нет проектов.")
        } else {
            myProjects.forEach { project ->
                ActivityCard {
                    Text(project.name, color = PetMatesTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(project.shortDescription, color = PetMatesTextSecondary, modifier = Modifier.padding(top = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onOpenProject(project.id) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White)
                        ) { Text("Открыть") }
                        OutlinedButton(onClick = { onEditProject(project.id) }, modifier = Modifier.weight(1f)) {
                            Text("Редактировать")
                        }
                    }
                    OutlinedButton(
                        onClick = { onCreateVacancy(project.id) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Добавить вакансию")
                    }
                }
            }
        }

        SectionTitle("Мои отклики")
        if (myResponses.isEmpty()) {
            EmptyActivityText("Вы пока не откликались на вакансии.")
        } else {
            myResponses.forEach { response ->
                ActivityCard {
                    Text(response.projectName, color = PetMatesPrimary, fontWeight = FontWeight.Bold)
                    Text("Отклик на роль «${response.vacancyTitle}»", color = PetMatesTextPrimary, modifier = Modifier.padding(top = 4.dp))
                    Text(response.statusLabel, color = PetMatesTextSecondary, modifier = Modifier.padding(top = 6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { onOpenVacancy(response.vacancyId) }, modifier = Modifier.weight(1f)) {
                            Text("Открыть")
                        }
                        if (response.isPending) {
                            OutlinedButton(
                                onClick = { onCancelResponse(response.responseId) },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, Danger),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger)
                            ) {
                                Text("Отменить")
                            }
                        }
                    }
                }
            }
        }

        SectionTitle("Входящие приглашения")
        if (myInvites.isEmpty()) {
            EmptyActivityText("Входящих приглашений пока нет.")
        } else {
            myInvites.forEach { invite ->
                ActivityCard {
                    Text(invite.projectName, color = PetMatesPrimary, fontWeight = FontWeight.Bold)
                    Text("Роль: ${invite.role}", color = PetMatesTextPrimary, modifier = Modifier.padding(top = 4.dp))
                    Text(invite.statusLabel, color = PetMatesTextSecondary, modifier = Modifier.padding(top = 6.dp))
                    if (invite.isPending) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onAcceptInvite(invite.inviteId) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White)
                            ) { Text("Принять") }
                            OutlinedButton(
                                onClick = { onDeclineInvite(invite.inviteId) },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, Danger),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger)
                            ) { Text("Отклонить") }
                        }
                    }
                }
            }
        }

        SectionTitle("Отправленные приглашения")
        if (sentInvites.isEmpty()) {
            EmptyActivityText("Вы пока никого не приглашали в свои проекты.")
        } else {
            sentInvites.forEach { invite ->
                ActivityCard {
                    Text(invite.projectName, color = PetMatesPrimary, fontWeight = FontWeight.Bold)
                    Text("Кому: @${invite.userName}", color = PetMatesTextPrimary, modifier = Modifier.padding(top = 4.dp))
                    Text("Роль: ${invite.role}", color = PetMatesTextPrimary, modifier = Modifier.padding(top = 4.dp))
                    Text("${invite.statusLabel}${if (invite.date.isNotBlank()) " • ${invite.date}" else ""}", color = PetMatesTextSecondary, modifier = Modifier.padding(top = 6.dp))
                    if (invite.isPending) {
                        OutlinedButton(
                            onClick = { onCancelSentInvite(invite.inviteId) },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            border = BorderStroke(1.dp, Danger),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger)
                        ) {
                            Text("Отменить приглашение")
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ActivityCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PetMatesSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun EmptyActivityText(text: String) {
    Text(text = text, color = PetMatesTextSecondary, modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = PetMatesTextPrimary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun Chip(text: String, background: Color) {
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = TagText, fontSize = 12.sp)
    }
}
@Composable
fun SettingsTab(
    accountName: String,
    modifier: Modifier = Modifier,
    onEditProfileClick: () -> Unit = {},
    onLogoutComplete: () -> Unit = {},
    vm: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by vm.state.collectAsState()
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                UiEvent.AuthRequired -> onLogoutComplete()
                is UiEvent.ShowMessage -> Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                else -> Unit
            }
        }
    }

    // Заменяем Box + LazyColumn на простой Column
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsHeader(accountName = accountName)

        SettingsCard(title = "Аккаунт") {
            SettingsActionRow(
                title = "Редактировать профиль",
                subtitle = "Имя, город, роль, навыки и контакты",
                onClick = onEditProfileClick,
            )
            HorizontalDivider()
            SettingsActionRow(
                title = "Сменить почту",
                subtitle = "Mock-сценарий до подключения Auth API",
                onClick = vm::changeEmail,
            )
            HorizontalDivider()
            SettingsActionRow(
                title = "Сменить пароль",
                subtitle = "Mock-сценарий до подключения Auth API",
                onClick = vm::changePassword,
            )
        }

        SettingsCard(title = "Уведомления") {
            SettingsSwitchRow(
                title = "Отклики",
                subtitle = "Новые отклики и изменение статуса",
                checked = state.responseNotifications,
                onCheckedChange = vm::setResponseNotifications,
            )
            HorizontalDivider()
            SettingsSwitchRow(
                title = "Приглашения",
                subtitle = "Входящие приглашения в проекты",
                checked = state.inviteNotifications,
                onCheckedChange = vm::setInviteNotifications,
            )
            HorizontalDivider()
            SettingsSwitchRow(
                title = "Проекты",
                subtitle = "Обновления статуса и активности",
                checked = state.projectNotifications,
                onCheckedChange = vm::setProjectNotifications,
            )
        }

        SettingsCard(title = "Безопасность") {
            SettingsActionRow(
                title = "Выйти из аккаунта",
                subtitle = "Завершить текущую mock-сессию",
                titleColor = Danger,
                onClick = { showLogoutDialog = true },
            )
        }

        SettingsCard(title = "Опасная зона", titleColor = Danger) {
            Text(
                text = "Удаление аккаунта пока работает как mock-заглушка. После подключения API здесь будет подтверждение и серверное удаление.",
                color = PetMatesTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Danger),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger)
            ) {
                Text("Удалить аккаунт", fontWeight = FontWeight.Bold)
            }
        }

        // Диалоги можно оставить внизу, так как они не являются частью скроллируемого потока
        if (showLogoutDialog) {
            LogoutDialog(
                isBusy = state.isBusy,
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    showLogoutDialog = false
                    vm.logout()
                }
            )
        }

        if (showDeleteDialog) {
            DeleteAccountDialog(accountName = accountName, onDismiss = { showDeleteDialog = false }, onConfirm = vm::deleteAccount)
        }
    }
}

@Composable
private fun SettingsHeader(accountName: String) {
    Column {
        Text("Настройки", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = PetMatesTextPrimary)
        Text(
            text = "Аккаунт @$accountName",
            color = PetMatesTextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    titleColor: Color = PetMatesTextPrimary,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PetMatesSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Text(
                text = title,
                color = titleColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            )
            content()
        }
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    titleColor: Color = PetMatesTextPrimary,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(title, color = titleColor, fontWeight = FontWeight.Bold)
            Text(subtitle, color = PetMatesTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = PetMatesTextPrimary, fontWeight = FontWeight.Bold)
            Text(subtitle, color = PetMatesTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun LogoutDialog(
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выйти из аккаунта?", fontWeight = FontWeight.Bold) },
        text = { Text("Текущая mock-сессия будет очищена, и вы вернётесь на экран авторизации.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isBusy,
                colors = ButtonDefaults.buttonColors(containerColor = Danger, contentColor = Color.White)
            ) {
                Text("Выйти")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBusy) {
                Text("Отмена", color = PetMatesTextSecondary)
            }
        }
    )
}

@Composable
private fun PlainField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
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
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggle: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PetMatesPrimary,
            cursorColor = PetMatesPrimary,
            unfocusedBorderColor = PetMatesTextSecondary.copy(alpha = 0.5f),
            focusedTextColor = PetMatesTextPrimary,
            unfocusedTextColor = PetMatesTextPrimary,
        )
    )
}

@Composable
private fun DeleteAccountDialog(
    accountName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Danger)
        },
        title = {
            Text("Удаление аккаунта", color = Danger, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Для подтверждения введите имя вашего аккаунта и код подтверждения из письма.",
                    color = PetMatesTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                PlainField(label = "Имя аккаунта (например, \"$accountName\")", value = name, onValueChange = { name = it })
                Spacer(modifier = Modifier.height(12.dp))
                PlainField(label = "Код подтверждения", value = code, onValueChange = { code = it })
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ErrorContainer, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Удаление пока не реализовано (UI-заглушка).",
                        color = Danger,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                enabled = name == accountName && code.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Danger, contentColor = Color.White)
            ) {
                Text("Удалить навсегда")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = PetMatesTextSecondary)
            }
        }
    )
}
