package com.kynzai.petmates.navigation

/**
 * Храним все пути навигации в одном месте, чтобы не размазывать "строки-роуты" по проекту.
 */
object Routes {
    const val Auth = "auth"

    const val MainFeed = "main_feed"

    const val Notifications = "notifications"

    // Сценарии создания/редактирования сущностей.
    const val CreateProject = "create_project"
    const val CreateVacancy = "create_vacancy"
    const val EditProject = "edit_project"
    const val EditVacancy = "edit_vacancy"
    const val VacancyDetails = "vacancy_details"
    const val ProfileEdit = "profile_edit"
    const val InviteUser = "invite_user"

    // Детальные экраны.
    const val ProjectDetails = "project_details"
    const val UserProfile = "user_profile"

    fun projectDetails(projectId: String): String = "$ProjectDetails/$projectId"
    fun userProfile(nickname: String): String = "$UserProfile/$nickname"

    fun createVacancy(projectId: String): String = "$CreateVacancy/$projectId"
    fun editProject(projectId: String): String = "$EditProject/$projectId"
    fun editVacancy(vacancyId: String): String = "$EditVacancy/$vacancyId"
    fun vacancyDetails(vacancyId: String): String = "$VacancyDetails/$vacancyId"
    fun inviteUser(projectId: String): String = "$InviteUser/$projectId"
}
