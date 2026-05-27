package com.kynzai.petmates.ui.main

import com.kynzai.data.BuildConfig as DataBuildConfig
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
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kynzai.petmates.ui.common.AuthRequiredScreen
import com.kynzai.petmates.ui.common.DemoModeBanner
import com.kynzai.petmates.ui.common.InDevelopmentScreen
import com.kynzai.petmates.ui.common.VacanciesPlaceholderScreen
import com.kynzai.petmates.ui.profile.ProfileScreen
import com.kynzai.petmates.ui.profile.ProfileViewModel
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary
import com.kynzai.petmates.ui.users.UsersRoute
import com.kynzai.petmates.ui.users.UsersViewModel

private enum class MainTab { Profile, Vacancies, Responses, Users }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isAuthorized: Boolean,
    onAuthRequested: () -> Unit,
    onNotificationsClick: () -> Unit,
    onCreateProjectClick: () -> Unit,
    onEditProfileClick: () -> Unit = {},
    onLogoutComplete: () -> Unit = {},
    onEditProjectClick: (String) -> Unit = {},
    onCreateVacancyClick: (String) -> Unit = {},
    onNavigateToVacancy: (String) -> Unit = {},
    onNavigateToProject: (String) -> Unit = {},
    onNavigateToUser: (String) -> Unit = {},
) {
    // Скоблим ViewModel на MainScreen, чтобы они не пересоздавались при переключении вкладок.
    val profileVm: ProfileViewModel = hiltViewModel()
    val usersVm: UsersViewModel = hiltViewModel()

    var selectedTab by rememberSaveable { mutableIntStateOf(MainTab.Vacancies.ordinal) }
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
                    selected = selectedTab == MainTab.Vacancies.ordinal,
                    label = "Заявки",
                    icon = Icons.Default.Event,
                    onClick = { selectedTab = MainTab.Vacancies.ordinal }
                )
                BottomItem(
                    selected = selectedTab == MainTab.Responses.ordinal,
                    label = "Отклики",
                    icon = Icons.Default.Description,
                    onClick = { selectedTab = MainTab.Responses.ordinal }
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
            Column(Modifier.fillMaxSize()) {
                if (DataBuildConfig.USE_MOCKS) {
                    DemoModeBanner()
                }
                Box(Modifier.weight(1f)) {
                    when (MainTab.entries.getOrNull(selectedTab) ?: MainTab.Vacancies) {
                        MainTab.Profile -> {
                            if (!isAuthorized) {
                                AuthRequiredScreen(
                                    message = "Войдите в аккаунт, чтобы открыть профиль и ваши проекты.",
                                    onAuthClick = onAuthRequested,
                                )
                            } else {
                                ProfileScreen(
                                    onCreateProjectClick = onCreateProjectClick,
                                    onEditProfileClick = onEditProfileClick,
                                    onLogoutComplete = onLogoutComplete,
                                    onAuthRequested = onAuthRequested,
                                    onOpenProject = onNavigateToProject,
                                    onEditProject = onEditProjectClick,
                                    onCreateVacancy = onCreateVacancyClick,
                                    onOpenVacancy = onNavigateToVacancy,
                                    vm = profileVm,
                                )
                            }
                        }

                        MainTab.Vacancies -> VacanciesPlaceholderScreen()
                        MainTab.Responses -> InDevelopmentScreen(
                            message = "Отклики и входящие заявки на вакансии пока в разработке.",
                        )
                        MainTab.Users -> UsersRoute(onUserClick = onNavigateToUser, vm = usersVm)
                    }
                }
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
