package com.kynzai.petmates.navigation

/**
 * Храним все пути навигации в одном месте, чтобы не размазывать "строки-роуты" по проекту.
 */
object Routes {
    const val Auth = "auth"

    const val MainFeed = "main_feed"

    const val Notifications = "notifications"

    // Details
    const val ProjectDetails = "project_details"
    const val UserProfile = "user_profile"

    fun projectDetails(projectId: String): String = "$ProjectDetails/$projectId"
    fun userProfile(nickname: String): String = "$UserProfile/$nickname"
}
