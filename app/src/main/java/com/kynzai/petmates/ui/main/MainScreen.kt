package com.kynzai.petmates.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kynzai.petmates.ui.events.EventsScreen
import com.kynzai.petmates.ui.profile.ProfileScreen
import com.kynzai.petmates.ui.requests.RequestsScreen
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary
import com.kynzai.petmates.ui.users.UsersTab

private enum class MainTab { Profile, Events, Requests, Users }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isAuthorized: Boolean,
    onAuthRequested: () -> Unit,
    onNotificationsClick: () -> Unit,
    onNavigateToProject: (String) -> Unit = {},
    onNavigateToUser: (String) -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(MainTab.Events.ordinal) }

    Scaffold(
        containerColor = PetMatesSurface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PetMates",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = PetMatesTextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Уведомления",
                            tint = PetMatesTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PetMatesSurface)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = PetMatesSurface) {
                BottomItem(
                    selected = selectedTab == MainTab.Profile.ordinal,
                    label = "Профиль",
                    icon = Icons.Default.Person,
                    onClick = { selectedTab = MainTab.Profile.ordinal }
                )
                BottomItem(
                    selected = selectedTab == MainTab.Events.ordinal,
                    label = "Мероприятия",
                    icon = Icons.Default.Event,
                    onClick = { selectedTab = MainTab.Events.ordinal }
                )
                BottomItem(
                    selected = selectedTab == MainTab.Requests.ordinal,
                    label = "Заявки",
                    icon = Icons.Default.Description,
                    onClick = { selectedTab = MainTab.Requests.ordinal }
                )
                BottomItem(
                    selected = selectedTab == MainTab.Users.ordinal,
                    label = "Участники",
                    icon = Icons.Default.Group,
                    onClick = { selectedTab = MainTab.Users.ordinal }
                )
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = PetMatesBackground
        ) {
            when (MainTab.entries.getOrNull(selectedTab) ?: MainTab.Events) {
                MainTab.Profile -> {
                    if (!isAuthorized) {
                        GuestProfilePlaceholder(onAuthRequested)
                    } else {
                        ProfileScreen()
                    }
                }

                MainTab.Events -> EventsScreen(onProjectClick = onNavigateToProject)
                MainTab.Requests -> RequestsScreen()
                MainTab.Users -> UsersTab(onUserClick = onNavigateToUser)
            }
        }
    }
}

@Composable
private fun RowScope.BottomItem(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    val selectedColor = PetMatesPrimary
    val unselectedColor = PetMatesTextSecondary
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) selectedColor else unselectedColor
            )
        },
        label = {
            Text(text = label, color = if (selected) selectedColor else unselectedColor)
        }
    )
}

@Composable
private fun GuestProfilePlaceholder(onAuthRequested: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Войдите в аккаунт, чтобы открыть профиль и ваши проекты.",
                color = PetMatesTextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = onAuthRequested,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PetMatesPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Авторизоваться", fontWeight = FontWeight.Bold)
            }
        }
    }
}
