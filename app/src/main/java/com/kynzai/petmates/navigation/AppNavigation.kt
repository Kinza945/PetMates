package com.kynzai.petmates.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kynzai.petmates.ui.auth.AuthScreen
import com.kynzai.petmates.ui.profile.UserProfileScreen
import com.kynzai.petmates.ui.project.ProjectDetailsScreen
import com.kynzai.petmates.ui.main.MainScreen
import com.kynzai.petmates.ui.notifications.NotificationsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var isAuthorized by rememberSaveable { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = Routes.Auth,
    ) {
        composable(Routes.Auth) {
            AuthScreen(
                onAuthorizedContinue = {
                    isAuthorized = true
                    // If Auth was opened from inside the app, just go back.
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.MainFeed) {
                            popUpTo(Routes.Auth) { inclusive = true }
                        }
                    }
                },
                onGuestContinue = {
                    isAuthorized = false
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
                onNavigateToProject = { projectId ->
                    navController.navigate(Routes.projectDetails(projectId))
                },
                onNavigateToUser = { nickname ->
                    navController.navigate(Routes.userProfile(nickname))
                }
            )
        }

        composable(Routes.Notifications) {
            NotificationsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(
            route = "${Routes.ProjectDetails}/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            ProjectDetailsScreen(
                projectId = backStackEntry.arguments?.getString("projectId"),
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.UserProfile}/{nickname}",
            arguments = listOf(navArgument("nickname") { type = NavType.StringType })
        ) { backStackEntry ->
            UserProfileScreen(
                nickname = backStackEntry.arguments?.getString("nickname"),
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
