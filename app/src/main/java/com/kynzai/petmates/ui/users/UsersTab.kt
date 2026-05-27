package com.kynzai.petmates.ui.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kynzai.domain.common.LoadState
import com.kynzai.petmates.ui.common.EmptyStateScreen
import com.kynzai.petmates.ui.common.ErrorStateScreen
import com.kynzai.petmates.ui.common.LoadingStateScreen
import com.kynzai.petmates.ui.common.ServerUnavailableScreen
import com.kynzai.petmates.ui.common.isServerUnavailable
import com.kynzai.petmates.ui.common.toUiMessage
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary
import kotlinx.coroutines.delay

@Composable
fun UsersRoute(
    onUserClick: (String) -> Unit,
    vm: UsersViewModel = hiltViewModel(),
) {
    var query by rememberSaveable { mutableStateOf("") }
    val state by vm.state.collectAsState()

    LaunchedEffect(query) {
        delay(250)
        vm.load(query)
    }

    UsersTab(
        query = query,
        onQueryChange = { query = it },
        users = (state as? LoadState.Data)?.value.orEmpty(),
        isLoading = state is LoadState.Loading,
        errorMessage = (state as? LoadState.Error)?.error?.toUiMessage(),
        isServerUnavailable = (state as? LoadState.Error)?.error?.isServerUnavailable() == true,
        onRetryClick = { vm.load(query, force = true) },
        onUserClick = onUserClick,
    )
}

@Composable
fun UsersTab(
    query: String,
    onQueryChange: (String) -> Unit,
    users: List<UserCardUi>,
    isLoading: Boolean,
    errorMessage: String? = null,
    isServerUnavailable: Boolean = false,
    onRetryClick: () -> Unit = {},
    onUserClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PetMatesBackground)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Найти по нику или навыку...", color = PetMatesTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PetMatesTextSecondary) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PetMatesPrimary,
                cursorColor = PetMatesPrimary,
                unfocusedBorderColor = PetMatesTextSecondary.copy(alpha = 0.5f),
                focusedTextColor = PetMatesTextPrimary,
                unfocusedTextColor = PetMatesTextPrimary,
            )
        )

        Spacer(modifier = Modifier.size(16.dp))

        when {
            isLoading && users.isEmpty() -> LoadingStateScreen(message = "Ищем участников...")
            isServerUnavailable -> ServerUnavailableScreen(onRetryClick = onRetryClick)
            errorMessage != null -> ErrorStateScreen(message = errorMessage, onRetryClick = onRetryClick)
            users.isEmpty() -> EmptyStateScreen(
                title = "Ничего не найдено",
                message = if (query.isBlank()) {
                    "Участники появятся после подключения данных."
                } else {
                    "Попробуйте изменить запрос поиска."
                },
            )
            else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(users, key = { it.nickname }) { user ->
                    UserCard(user = user, onClick = { onUserClick(user.nickname) })
                }
            }
            }
        }
    }
}

@Composable
private fun UserCard(
    user: UserCardUi,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PetMatesSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(50.dp).background(Color.LightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.nickname, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PetMatesTextPrimary)
                Text(text = user.role, color = PetMatesTextSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.size(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    user.skills.take(3).forEach { skill ->
                        val tag = if (skill.startsWith("#")) skill else "#$skill"
                        Text(
                            text = tag,
                            color = PetMatesPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
