package com.kynzai.petmates.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kynzai.petmates.ui.common.ScreenState
import com.kynzai.petmates.ui.common.UiEvent
import com.kynzai.petmates.ui.mappers.ProjectUi
import com.kynzai.petmates.ui.mappers.UserUi
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = PetMatesTextPrimary)
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
                    data = s.value.user,
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
    data: UserUi,
    isAuthorized: Boolean,
    modifier: Modifier,
    onInviteClick: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.LightGray),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Column(Modifier.padding(start = 16.dp)) {
                    Text(data.nickname, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = PetMatesTextPrimary)
                    Text(data.realName, color = PetMatesTextSecondary)
                    Text(listOf(data.country, data.city).filter { it.isNotBlank() }.joinToString(", "), color = PetMatesTextSecondary, fontSize = 14.sp)
                }
            }
        }
        item {
            Text("О себе", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PetMatesTextPrimary)
            Text(data.description.ifBlank { "Описание пока не заполнено" }, color = PetMatesTextPrimary, modifier = Modifier.padding(top = 8.dp))
        }
        item {
            Text("Hard-skills", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                data.hardSkills.forEach { AssistChip(onClick = {}, label = { Text(it) }) }
            }
        }
        item {
            Text("Soft-skills", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                data.softSkills.forEach { AssistChip(onClick = {}, label = { Text(it) }) }
            }
        }
        item {
            Text("Контакты", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary)
            data.contacts.forEach { contact ->
                Text("${contact.name}: ${contact.link}", color = PetMatesPrimary, modifier = Modifier.padding(top = 6.dp))
            }
        }
        item {
            Button(
                onClick = onInviteClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White),
            ) {
                Text(if (isAuthorized) "Пригласить в проект" else "Авторизоваться для приглашения", fontWeight = FontWeight.Bold)
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
