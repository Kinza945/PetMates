package com.kynzai.petmates.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kynzai.petmates.ui.auth.AuthScreen
import com.kynzai.petmates.ui.invite.InviteUserScreen
import com.kynzai.petmates.ui.profile.UserProfileScreen
import com.kynzai.petmates.ui.project.ProjectDetailsScreen
import com.kynzai.petmates.ui.project.CreateProjectScreen
import com.kynzai.petmates.ui.main.MainScreen
import com.kynzai.petmates.ui.notifications.NotificationsScreen
import com.kynzai.petmates.ui.vacancy.CreateVacancyScreen
import com.kynzai.petmates.session.SessionManager

@Composable
fun AppNavigation(
    sessionManager: SessionManager,
) {
    val navController = rememberNavController()
    val session by sessionManager.state.collectAsState()
    val isAuthorized = session.isAuthorized

    NavHost(
        navController = navController,
        startDestination = Routes.Auth,
    ) {
        composable(Routes.Auth) {
            AuthScreen(
                onAuthorizedContinue = { nickname, email ->
                    sessionManager.authorizeAs(nickname = nickname, email = email)
                    // If Auth was opened from inside the app, just go back.
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.MainFeed) {
                            popUpTo(Routes.Auth) { inclusive = true }
                        }
                    }
                },
                onGuestContinue = {
                    sessionManager.continueAsGuest()
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.MainFeed) {
                            popUpTo(Routes.Auth) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.MainFeed) {
            MainScreen(
                isAuthorized = isAuthorized,
                onAuthRequested = { navController.navigate(Routes.Auth) },
                onNotificationsClick = { navController.navigate(Routes.Notifications) },
                onCreateProjectClick = { navController.navigate(Routes.CreateProject) },
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
