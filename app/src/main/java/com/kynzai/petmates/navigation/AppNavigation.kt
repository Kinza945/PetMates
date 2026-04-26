package com.kynzai.petmates.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kynzai.domain.models.LoginRequest
import com.kynzai.domain.models.RegisterRequest
import com.kynzai.petmates.ui.auth.AuthScreen
import com.kynzai.petmates.ui.invite.InviteUserScreen
import com.kynzai.petmates.ui.profile.UserProfileScreen
import com.kynzai.petmates.ui.profile.ProfileEditScreen
import com.kynzai.petmates.ui.project.ProjectDetailsScreen
import com.kynzai.petmates.ui.project.CreateProjectScreen
import com.kynzai.petmates.ui.project.EditProjectScreen
import com.kynzai.petmates.ui.main.MainScreen
import com.kynzai.petmates.ui.notifications.NotificationsScreen
import com.kynzai.petmates.ui.vacancy.CreateVacancyScreen
import com.kynzai.petmates.ui.vacancy.EditVacancyScreen
import com.kynzai.petmates.ui.vacancy.VacancyDetailsScreen
import com.kynzai.petmates.session.SessionManager
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    sessionManager: SessionManager,
) {
    val navController = rememberNavController()
    val session by sessionManager.state.collectAsState()
    val isAuthorized = session.isAuthorized
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun navigateAfterAuth() {
        if (!navController.popBackStack()) {
            navController.navigate(Routes.MainFeed) {
                popUpTo(Routes.Auth) { inclusive = true }
            }
        }
    }

    fun showAuthError(error: Throwable) {
        Toast.makeText(
            context,
            error.message ?: "Не удалось выполнить вход",
            Toast.LENGTH_SHORT,
        ).show()
    }

    NavHost(
        navController = navController,
        startDestination = if (isAuthorized) Routes.MainFeed else Routes.Auth,
    ) {
        composable(Routes.Auth) {
            AuthScreen(
                onLogin = { request ->
                    scope.launch {
                        sessionManager.login(request)
                            .onSuccess { navigateAfterAuth() }
                            .onFailure(::showAuthError)
                    }
                },
                onRegister = { request ->
                    scope.launch {
                        sessionManager.register(request)
                            .onSuccess { navigateAfterAuth() }
                            .onFailure(::showAuthError)
                    }
                },
                onGuestContinue = {
                    scope.launch {
                        sessionManager.continueAsGuest()
                        navigateAfterAuth()
                    }
                },
                onSocialAuth = { nickname, email ->
                    scope.launch {
                        val login = sessionManager.login(
                            LoginRequest(
                                nicknameOrEmail = nickname,
                                password = "mock-social",
                                rememberMe = true,
                            )
                        )
                        val result = if (login.isSuccess) {
                            login
                        } else {
                            sessionManager.register(
                                RegisterRequest(
                                    nickname = nickname,
                                    email = email,
                                    password = "mock-social",
                                )
                            )
                        }

                        result
                            .onSuccess { navigateAfterAuth() }
                            .onFailure(::showAuthError)
                    }
                },
            )
        }

        composable(Routes.MainFeed) {
            MainScreen(
                isAuthorized = isAuthorized,
                onAuthRequested = { navController.navigate(Routes.Auth) },
                onNotificationsClick = { navController.navigate(Routes.Notifications) },
                onCreateProjectClick = { navController.navigate(Routes.CreateProject) },
                onEditProfileClick = { navController.navigate(Routes.ProfileEdit) },
                onLogoutComplete = {
                    navController.navigate(Routes.Auth) {
                        popUpTo(Routes.MainFeed) { inclusive = true }
                    }
                },
                onEditProjectClick = { projectId -> navController.navigate(Routes.editProject(projectId)) },
                onCreateVacancyClick = { projectId -> navController.navigate(Routes.createVacancy(projectId)) },
                onNavigateToVacancy = { vacancyId -> navController.navigate(Routes.vacancyDetails(vacancyId)) },
                onNavigateToProject = { projectId ->
                    navController.navigate(Routes.projectDetails(projectId))
                },
                onNavigateToUser = { nickname ->
                    navController.navigate(Routes.userProfile(nickname))
                }
            )
        }

        composable(Routes.Notifications) {
            NotificationsScreen(
                isAuthorized = isAuthorized,
                onBackClick = { navController.popBackStack() },
                onAuthRequested = { navController.navigate(Routes.Auth) },
            )
        }

        composable(Routes.CreateProject) {
            CreateProjectScreen(
                onBackClick = { navController.popBackStack() },
                onCreated = { projectId ->
                    navController.navigate(Routes.projectDetails(projectId)) {
                        popUpTo(Routes.CreateProject) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ProfileEdit) {
            ProfileEditScreen(
                onBackClick = { navController.popBackStack() },
                onAuthRequested = { navController.navigate(Routes.Auth) },
            )
        }

        composable(
            route = "${Routes.EditProject}/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            EditProjectScreen(
                projectId = backStackEntry.arguments?.getString("projectId"),
                onBackClick = { navController.popBackStack() },
                onAuthRequested = { navController.navigate(Routes.Auth) },
            )
        }

        composable(
            route = "${Routes.EditVacancy}/{vacancyId}",
            arguments = listOf(navArgument("vacancyId") { type = NavType.StringType })
        ) { backStackEntry ->
            EditVacancyScreen(
                vacancyId = backStackEntry.arguments?.getString("vacancyId"),
                onBackClick = { navController.popBackStack() },
                onAuthRequested = { navController.navigate(Routes.Auth) },
            )
        }

        composable(
            route = "${Routes.VacancyDetails}/{vacancyId}",
            arguments = listOf(navArgument("vacancyId") { type = NavType.StringType })
        ) { backStackEntry ->
            VacancyDetailsScreen(
                vacancyId = backStackEntry.arguments?.getString("vacancyId"),
                onBackClick = { navController.popBackStack() },
                onAuthRequested = { navController.navigate(Routes.Auth) },
            )
        }

        composable(
            route = "${Routes.CreateVacancy}/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            CreateVacancyScreen(
                projectId = backStackEntry.arguments?.getString("projectId"),
                onBackClick = { navController.popBackStack() },
                onCreated = { navController.popBackStack() },
                isAuthorized = isAuthorized,
                onAuthRequested = { navController.navigate(Routes.Auth) },
            )
        }

        composable(
            route = "${Routes.InviteUser}/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            InviteUserScreen(
                projectId = backStackEntry.arguments?.getString("projectId"),
                onBackClick = { navController.popBackStack() },
                onInvited = { navController.popBackStack() },
                isAuthorized = isAuthorized,
                onAuthRequested = { navController.navigate(Routes.Auth) },
            )
        }

        composable(
            route = "${Routes.ProjectDetails}/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            ProjectDetailsScreen(
                projectId = projectId,
                onBackClick = { navController.popBackStack() },
                isAuthorized = isAuthorized,
                onAuthRequested = { navController.navigate(Routes.Auth) },
                onCreateVacancyClick = { id -> navController.navigate(Routes.createVacancy(id)) },
                onInviteUserClick = { id -> navController.navigate(Routes.inviteUser(id)) },
                onEditProjectClick = { id -> navController.navigate(Routes.editProject(id)) },
                onVacancyClick = { id -> navController.navigate(Routes.vacancyDetails(id)) },
            )
        }

        composable(
            route = "${Routes.UserProfile}/{nickname}",
            arguments = listOf(navArgument("nickname") { type = NavType.StringType })
        ) { backStackEntry ->
            UserProfileScreen(
                nickname = backStackEntry.arguments?.getString("nickname"),
                onBackClick = { navController.popBackStack() },
                isAuthorized = isAuthorized,
                onAuthRequested = { navController.navigate(Routes.Auth) },
                onInviteToProject = { projectId: String -> navController.navigate(Routes.inviteUser(projectId)) },
            )
        }
    }
}
