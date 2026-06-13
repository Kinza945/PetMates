package com.kynzai.petmates.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.domain.models.InviteStatus
import com.kynzai.domain.models.ResponseStatus
import com.kynzai.domain.models.User
import com.kynzai.domain.repositories.InviteRepository
import com.kynzai.domain.repositories.ProjectRepository
import com.kynzai.domain.repositories.ResponseRepository
import com.kynzai.domain.repositories.UserRepository
import com.kynzai.petmates.session.SessionManager
import com.kynzai.petmates.ui.common.UiEvent
import com.kynzai.petmates.ui.mappers.ProjectUi
import com.kynzai.petmates.ui.mappers.toUi
import com.kynzai.petmates.ui.mappers.toUiLabel
import com.kynzai.petmates.ui.mappers.toUuidOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class ProfileUiState(
    val data: User? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val isUnauthorized: Boolean = false,
    val myProjects: List<ProjectUi> = emptyList(),
    val myResponses: List<ProfileResponseUi> = emptyList(),
    val myInvites: List<ProfileInviteUi> = emptyList(),
    val sentInvites: List<ProfileSentInviteUi> = emptyList(),
)

data class ProfileResponseUi(
    val responseId: String,
    val vacancyId: String,
    val projectName: String,
    val vacancyTitle: String,
    val statusLabel: String,
    val isPending: Boolean,
)

data class ProfileInviteUi(
    val inviteId: String,
    val projectId: String,
    val projectName: String,
    val role: String,
    val statusLabel: String,
    val isPending: Boolean,
)

data class ProfileSentInviteUi(
    val inviteId: String,
    val projectId: String,
    val projectName: String,
    val userName: String,
    val role: String,
    val statusLabel: String,
    val date: String,
    val isPending: Boolean,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val users: UserRepository,
    private val projects: ProjectRepository,
    private val responses: ResponseRepository,
    private val invites: InviteRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    private var refreshJob: Job? = null

    fun refresh(force: Boolean = false) {
        val me = sessionManager.state.value.currentUserId
        if (me == null) {
            _state.value = ProfileUiState(isLoading = false, isUnauthorized = true)
            return
        }

        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            /*
             * Текущий профиль грузим по id из сессии, а не через /api/profile/me.
             * Сейчас backend на 78.17.198.221 отдаёт сайт, а не JSON API, поэтому
             * real-репозиторий сможет прозрачно откатиться на Supabase users.
             * Когда backend даст /api/profile/me, этот обход можно убрать.
             */
            users.getUserById(me)
                .onSuccess { user ->
                    val activity = buildProfileActivity(user)
                    _state.value = ProfileUiState(
                        data = user,
                        isLoading = false,
                        isRefreshing = false,
                        error = null,
                        myProjects = activity.myProjects,
                        myResponses = activity.myResponses,
                        myInvites = activity.myInvites,
                        sentInvites = activity.sentInvites,
                    )
                }
                .onFailure { error ->
                    _state.value = ProfileUiState(
                        isLoading = false,
                        error = error.message ?: "Не удалось загрузить профиль",
                    )
                }
        }
    }

    fun cancelResponse(responseId: String) {
        val id = responseId.toUuidOrNull() ?: return
        viewModelScope.launch {
            responses.cancelResponse(id)
                .onSuccess {
                    _events.emit(UiEvent.ShowMessage("Отклик отменён"))
                    refresh(force = true)
                }
                .onFailure {
                    _events.emit(UiEvent.ShowMessage(it.message ?: "Не удалось отменить отклик"))
                }
        }
    }

    fun acceptInvite(inviteId: String) {
        updateInvite(inviteId, InviteStatus.ACCEPTED, "Приглашение принято")
    }

    fun declineInvite(inviteId: String) {
        updateInvite(inviteId, InviteStatus.DECLINED, "Приглашение отклонено")
    }

    fun cancelSentInvite(inviteId: String) {
        val id = inviteId.toUuidOrNull() ?: return
        viewModelScope.launch {
            invites.cancelInvite(id)
                .onSuccess {
                    _events.emit(UiEvent.ShowMessage("Приглашение отменено"))
                    refresh(force = true)
                }
                .onFailure {
                    _events.emit(UiEvent.ShowMessage(it.message ?: "Не удалось отменить приглашение"))
                }
        }
    }

    private fun updateInvite(inviteId: String, status: InviteStatus, message: String) {
        val id = inviteId.toUuidOrNull() ?: return
        viewModelScope.launch {
            invites.updateInviteStatus(id, status)
                .onSuccess {
                    _events.emit(UiEvent.ShowMessage(message))
                    refresh(force = true)
                }
                .onFailure {
                    _events.emit(UiEvent.ShowMessage(it.message ?: "Не удалось обновить приглашение"))
                }
        }
    }

    private data class ProfileActivity(
        val myProjects: List<ProjectUi>,
        val myResponses: List<ProfileResponseUi>,
        val myInvites: List<ProfileInviteUi>,
        val sentInvites: List<ProfileSentInviteUi>,
    )

    private suspend fun buildProfileActivity(user: User): ProfileActivity {
        val allProjects = projects.getAllProjects().getOrElse {
            _events.emit(UiEvent.ShowMessage(it.message ?: "Не удалось загрузить проекты"))
            emptyList()
        }
        val myProjects = allProjects.filter { it.ownerId == user.userId }
        val myResponses = buildMyResponses(userId = user.userId)
        val myInvites = invites.getInvitesByUser(user.userId)
            .getOrDefault(emptyList())
            .map { invite ->
                val project = allProjects.firstOrNull { it.projectId == invite.projectId }
                ProfileInviteUi(
                    inviteId = invite.inviteId.toString(),
                    projectId = invite.projectId.toString(),
                    projectName = project?.name ?: "Неизвестный проект",
                    role = invite.role,
                    statusLabel = invite.status.toUiLabel(),
                    isPending = invite.status == InviteStatus.PENDING,
                )
            }
        val sentInvites = myProjects.flatMap { project ->
            invites.getInvitesByProject(project.projectId)
                .getOrDefault(emptyList())
                .map { invite ->
                    ProfileSentInviteUi(
                        inviteId = invite.inviteId.toString(),
                        projectId = invite.projectId.toString(),
                        projectName = project.name,
                        userName = users.getUserById(invite.userId).getOrNull()?.nickname ?: "Unknown",
                        role = invite.role,
                        statusLabel = invite.status.toUiLabel(),
                        date = invite.createdAt?.toString()?.take(10).orEmpty(),
                        isPending = invite.status == InviteStatus.PENDING,
                    )
                }
        }.sortedByDescending { it.date }

        return ProfileActivity(
            myProjects = myProjects.map { it.toUi() },
            myResponses = myResponses,
            myInvites = myInvites,
            sentInvites = sentInvites,
        )
    }

    private suspend fun buildMyResponses(userId: java.util.UUID): List<ProfileResponseUi> {
        // ResponseRepository умеет читать отклики только по вакансии, поэтому пока
        // проходим по проектам и вакансиям. Для real API нужен endpoint "мои отклики".
        val result = mutableListOf<ProfileResponseUi>()
        val allProjects = projects.getAllProjects().getOrDefault(emptyList())
        allProjects.forEach { project ->
            val vacancies = projects.getProjectVacancies(project.projectId).getOrDefault(emptyList())
            vacancies.forEach { vacancy ->
                val vacancyResponses = responses.getResponsesByVacancy(vacancy.vacancyId).getOrDefault(emptyList())
                vacancyResponses.filter { it.userId == userId }.forEach { response ->
                    result.add(
                        ProfileResponseUi(
                            responseId = response.responseId.toString(),
                            vacancyId = vacancy.vacancyId.toString(),
                            projectName = project.name,
                            vacancyTitle = vacancy.title,
                            statusLabel = response.status.toUiLabel(),
                            isPending = response.status == ResponseStatus.PENDING,
                        )
                    )
                }
            }
        }
        return result
    }
}

private fun InviteStatus.toUiLabel(): String =
    when (this) {
        InviteStatus.PENDING -> "В ожидании"
        InviteStatus.ACCEPTED -> "Принято"
        InviteStatus.DECLINED -> "Отклонено"
        InviteStatus.CANCELLED -> "Отменено"
    }
