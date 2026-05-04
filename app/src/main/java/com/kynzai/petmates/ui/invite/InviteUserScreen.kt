package com.kynzai.petmates.ui.invite

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kynzai.domain.common.LoadState
import com.kynzai.domain.models.User
import com.kynzai.petmates.ui.common.AuthRequiredScreen
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteUserScreen(
    projectId: String?,
    onBackClick: () -> Unit,
    onInvited: () -> Unit,
    isAuthorized: Boolean,
    onAuthRequested: () -> Unit,
    vm: InviteUserViewModel = hiltViewModel(),
) {
    val parsed = runCatching { projectId?.let(UUID::fromString) }.getOrNull()

    LaunchedEffect(Unit) {
        // Первичная загрузка списка пользователей для приглашения.
        if (vm.usersState is LoadState.Idle) vm.search()
    }

    LaunchedEffect(vm.inviteState) {
        if (vm.inviteState is LoadState.Data) {
            onInvited()
            vm.consumeCreated()
        }
    }

    Scaffold(
        containerColor = PetMatesBackground,
        topBar = {
            TopAppBar(
                title = { Text("Пригласить участника", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = PetMatesTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PetMatesSurface)
            )
        },
        bottomBar = {
            if (isAuthorized && parsed != null) {
                val selectedUser = (vm.usersState as? LoadState.Data)
                    ?.value
                    ?.firstOrNull { it.userId == vm.selectedUserId }
                InviteBottomBar(
                    selectedUserName = selectedUser?.nickname,
                    role = vm.role,
                    message = vm.message,
                    isSubmitEnabled = vm.isValid && vm.inviteState !is LoadState.Loading,
                    onRoleChange = vm::updateRole,
                    onMessageChange = vm::updateMessage,
                    onSubmit = { vm.submit(parsed) },
                )
            }
        },
    ) { padding ->
        if (!isAuthorized) {
            AuthRequiredScreen(
                message = "Войдите, чтобы приглашать участников.",
                onAuthClick = onAuthRequested,
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }

        if (parsed == null) {
            Text(
                text = "Некорректный projectId",
                color = PetMatesTextSecondary,
                modifier = Modifier.padding(padding).padding(16.dp)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(PetMatesBackground)
                .padding(padding)
                .testTag("invite_user_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Поиск пользователя",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PetMatesTextPrimary
                )
            }

            item {
                OutlinedTextField(
                    value = vm.query,
                    onValueChange = vm::updateQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invite_search"),
                    label = { Text("Ник / роль / навык") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PetMatesTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PetMatesPrimary,
                        cursorColor = PetMatesPrimary,
                        unfocusedBorderColor = PetMatesTextSecondary.copy(alpha = 0.5f),
                        focusedTextColor = PetMatesTextPrimary,
                        unfocusedTextColor = PetMatesTextPrimary,
                    )
                )
            }

            item {
                Button(
                    onClick = vm::search,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White),
                ) {
                    Text("Найти", fontWeight = FontWeight.Bold)
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            when (val st = vm.usersState) {
                is LoadState.Loading -> item { Text("Загрузка...", color = PetMatesTextSecondary) }
                is LoadState.Error -> item { Text("Ошибка загрузки", color = PetMatesTextSecondary) }
                is LoadState.Data -> {
                    items(st.value, key = { it.userId }) { user ->
                        UserPickCard(
                            user = user,
                            selected = vm.selectedUserId == user.userId,
                            onClick = { vm.selectUser(user.userId) }
                        )
                    }
                }

                else -> item { Text("Введите запрос и нажмите «Найти»", color = PetMatesTextSecondary) }
            }

            item { Spacer(modifier = Modifier.height(160.dp)) }
        }
    }
}

@Composable
private fun InviteBottomBar(
    selectedUserName: String?,
    role: String,
    message: String,
    isSubmitEnabled: Boolean,
    onRoleChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = PetMatesSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = selectedUserName?.let { "Выбран: @$it" } ?: "Выберите пользователя из списка",
                color = if (selectedUserName == null) PetMatesTextSecondary else PetMatesTextPrimary,
                fontWeight = FontWeight.Bold,
            )
            OutlinedTextField(
                value = role,
                onValueChange = onRoleChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("invite_role"),
                label = { Text("Роль*") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PetMatesPrimary,
                    cursorColor = PetMatesPrimary,
                    unfocusedBorderColor = PetMatesTextSecondary.copy(alpha = 0.5f),
                    focusedTextColor = PetMatesTextPrimary,
                    unfocusedTextColor = PetMatesTextPrimary,
                )
            )
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("invite_message"),
                label = { Text("Сообщение") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PetMatesPrimary,
                    cursorColor = PetMatesPrimary,
                    unfocusedBorderColor = PetMatesTextSecondary.copy(alpha = 0.5f),
                    focusedTextColor = PetMatesTextPrimary,
                    unfocusedTextColor = PetMatesTextPrimary,
                )
            )
            Button(
                onClick = onSubmit,
                enabled = isSubmitEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(top = 8.dp)
                    .testTag("invite_submit"),
                colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White),
            ) {
                Text("Отправить приглашение", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun UserPickCard(
    user: User,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border = if (selected) PetMatesPrimary else PetMatesTextSecondary.copy(alpha = 0.35f)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PetMatesSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.LightGray, CircleShape)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.nickname, fontWeight = FontWeight.Bold, color = PetMatesTextPrimary)
                Text(
                    text = user.profileRole.orEmpty(),
                    color = PetMatesTextSecondary,
                    fontSize = 12.sp,
                )
            }
            if (selected) {
                Text(text = "Выбрано", color = PetMatesPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
