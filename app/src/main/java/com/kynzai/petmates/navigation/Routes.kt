package com.kynzai.petmates.navigation

/**
 * Храним все пути навигации в одном месте, чтобы не размазывать "строки-роуты" по проекту.
 */
object Routes {
    const val Auth = "auth"

    const val MainFeed = "main_feed"

    const val Notifications = "notifications"

    // Create flows
    const val CreateProject = "create_project"
    const val CreateVacancy = "create_vacancy"
    const val InviteUser = "invite_user"

    // Details
    const val ProjectDetails = "project_details"
    const val UserProfile = "user_profile"

    fun projectDetails(projectId: String): String = "$ProjectDetails/$projectId"
    fun userProfile(nickname: String): String = "$UserProfile/$nickname"

    fun createVacancy(projectId: String): String = "$CreateVacancy/$projectId"
    fun inviteUser(projectId: String): String = "$InviteUser/$projectId"
}
