package com.kynzai.petmates.ui.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun ProfileScreen() {
    // Keeping "current data" as-is (hardcoded), but layout is now mobile-friendly.
    val nickname = "DogI1X"
    val realName = "Гринькин Вадим"
    val role = "Python Data Science"
    val age = "21 год"
    val country = "Россия"
    val city = "Краснодар"
    val workplace = "ИМСИТ"
    val description =
        "Добавлю немного описания, чтобы быть самым модным на районе. Буду рад, если смогу научиться чему-нибудь интересному."
    val contacts = listOf(
        "Telegram" to "https://t.me/dog_i1x",
        "VK" to "https://vk.com/dog_i1x",
        "GitHub" to "https://github.com/dog_i1x",
        "YouTube" to "https://www.youtube.com/@spektr.project",
        "Portfolio" to "https://example.com/portfolio",
    )
    val hardSkills = listOf(
        "#kotlin",
        "#compose",
        "#ktor",
        "#csharp",
        "#rest",
        "#patterns",
        "#ui/ux",
        "#git",
        "#design",
        "#databases",
        "#postgres",
        "#supabase",
    )
    val softSkills = listOf("#communication", "#teamwork", "#ownership", "#curiosity", "#величайший")

    var selectedTab by rememberSaveable { mutableIntStateOf(ProfileTab.Info.ordinal) }
    val safeTabIndex = selectedTab.coerceIn(0, ProfileTab.entries.lastIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PetMatesSurface)
    ) {
        // Header (centered)
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
                text = nickname,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PetMatesTextPrimary
            )
            Text(
                text = realName,
                fontSize = 14.sp,
                color = PetMatesTextSecondary
            )
            Text(
                text = role,
                fontSize = 16.sp,
                color = PetMatesPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )

            FlowRow(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedInfoChip(country)
                OutlinedInfoChip(city)
                OutlinedInfoChip(workplace)
            }

            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "• онлайн", color = OnlineGreen, fontSize = 12.sp)
                Text(
                    text = "  $age • $city • $workplace",
                    color = PetMatesTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        // Tab content must have bounded height to avoid nested-scroll infinity constraints.
        Box(modifier = Modifier.weight(1f)) {
            when (ProfileTab.entries.getOrNull(selectedTab) ?: ProfileTab.Info) {
                ProfileTab.Info -> InfoTab(
                    modifier = Modifier.fillMaxSize(),
                    description = description,
                    contacts = contacts,
                    hardSkills = hardSkills,
                    softSkills = softSkills,
                )

                ProfileTab.Activity -> PlaceholderTab(
                    modifier = Modifier.fillMaxSize(),
                    title = "Активность"
                )

                ProfileTab.Notifications -> NotificationsTab(modifier = Modifier.fillMaxSize())
                ProfileTab.Settings -> SettingsTab(accountName = nickname, modifier = Modifier.fillMaxSize())
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
    contacts: List<Pair<String, String>>,
    hardSkills: List<String>,
    softSkills: List<String>,
    modifier: Modifier = Modifier,
) {
    val tagBg = PetMatesPrimary.copy(alpha = 0.25f)

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionTitle("Описание:")
            Text(text = description, color = PetMatesTextPrimary, fontSize = 14.sp)
        }

        item {
            SectionTitle("Для связи:")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                contacts.forEach { (name, link) ->
                    Text(
                        text = "$name: $link",
                        color = PetMatesTextPrimary,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item {
            SectionTitle("hard-skills:")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                hardSkills.forEach { Chip(text = it, background = tagBg) }
            }
        }

        item {
            SectionTitle("soft-skills:")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                softSkills.forEach { Chip(text = it, background = tagBg) }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
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
) {
    var oldPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var currentEmail by rememberSaveable { mutableStateOf("") }
    var newEmail by rememberSaveable { mutableStateOf("") }
    var emailCode by rememberSaveable { mutableStateOf("") }

    var oldVisible by rememberSaveable { mutableStateOf(false) }
    var newVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("profile_settings_tab"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Безопасность",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PetMatesTextPrimary,
                )
            }

            item {
                Text(text = "Смена пароля:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PetMatesTextPrimary)
            }

            item {
                PasswordField(
                    label = "Старый пароль",
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    visible = oldVisible,
                    onToggle = { oldVisible = !oldVisible }
                )
            }
            item {
                PasswordField(
                    label = "Новый пароль",
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    visible = newVisible,
                    onToggle = { newVisible = !newVisible }
                )
            }
            item {
                PasswordField(
                    label = "Подтверждение пароля",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    visible = confirmVisible,
                    onToggle = { confirmVisible = !confirmVisible }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Смена почты:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PetMatesTextPrimary
                )
            }

            item { PlainField(label = "Текущая почта", value = currentEmail, onValueChange = { currentEmail = it }) }
            item { PlainField(label = "Новая почта", value = newEmail, onValueChange = { newEmail = it }) }
            item { PlainField(label = "Код подтверждения", value = emailCode, onValueChange = { emailCode = it }) }

            item {
                Button(
                    onClick = { /* TODO save */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White)
                ) {
                    Text(text = "Сохранить изменения", fontWeight = FontWeight.Bold)
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp)) }

            item {
                Text(
                    text = "Удаление аккаунта",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Danger
                )
                Text(
                    text = "Это действие необратимо. Ваш аккаунт будет удалён навсегда со всеми данными.",
                    fontSize = 14.sp,
                    color = PetMatesTextSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Danger),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger)
                ) {
                    Text("Удалить аккаунт", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showDeleteDialog) {
            DeleteAccountDialog(
                accountName = accountName,
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
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
) {
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
                onClick = { /* TODO delete */ },
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
