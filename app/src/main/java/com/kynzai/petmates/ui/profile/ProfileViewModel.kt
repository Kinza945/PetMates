package com.kynzai.petmates.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.domain.models.InviteStatus
import com.kynzai.domain.models.ResponseStatus
import com.kynzai.domain.repositories.InviteRepository
import com.kynzai.domain.repositories.ProjectRepository
import com.kynzai.domain.repositories.ResponseRepository
import com.kynzai.domain.repositories.UserRepository
import com.kynzai.petmates.session.SessionManager
import com.kynzai.petmates.ui.common.ScreenState
import com.kynzai.petmates.ui.common.UiEvent
import com.kynzai.petmates.ui.mappers.ProjectUi
import com.kynzai.petmates.ui.mappers.UserUi
import com.kynzai.petmates.ui.mappers.toUi
import com.kynzai.petmates.ui.mappers.toUiLabel
import com.kynzai.petmates.ui.mappers.toUuidOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserUi,
    val myProjects: List<ProjectUi>,
    val myResponses: List<ProfileResponseUi>,
    val myInvites: List<ProfileInviteUi>,
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

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val users: UserRepository,
    private val projects: ProjectRepository,
    private val responses: ResponseRepository,
    private val invites: InviteRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow<ScreenState<ProfileUiState>>(ScreenState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    fun refresh() {
        val me = sessionManager.state.value.currentUserId
        if (me == null) {
            // Профиль текущего пользователя — приватный экран, гостю показываем AuthRequiredScreen.
            _state.value = ScreenState.Unauthorized
            return
        }

        viewModelScope.launch {
            _state.value = ScreenState.Loading
            val user = users.getUserById(me).getOrElse {
                _state.value = ScreenState.Error(it.message ?: "Не удалось загрузить профиль")
                return@launch
            }
            // На mock-этапе собираем агрегированную модель профиля на клиенте.
            // После появления backend это желательно заменить отдельным endpoint/RPC.
            val allProjects = projects.getAllProjects().getOrElse {
                _state.value = ScreenState.Error(it.message ?: "Не удалось загрузить проекты")
                return@launch
            }
            val myProjects = allProjects.filter { it.ownerId == me }
            val myResponses = buildMyResponses(userId = me)
            val myInvites = invites.getInvitesByUser(me)
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

            _state.value = ScreenState.Content(
                ProfileUiState(
                    user = user.toUi(),
                    myProjects = myProjects.map { it.toUi() },
                    myResponses = myResponses,
                    myInvites = myInvites,
                )
            )
        }
    }

    fun cancelResponse(responseId: String) {
        val id = responseId.toUuidOrNull() ?: return
        viewModelScope.launch {
            responses.cancelResponse(id)
                .onSuccess {
                    _events.emit(UiEvent.ShowMessage("Отклик отменён"))
                    refresh()
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

    private fun updateInvite(inviteId: String, status: InviteStatus, message: String) {
        val id = inviteId.toUuidOrNull() ?: return
        viewModelScope.launch {
            invites.updateInviteStatus(id, status)
                .onSuccess {
                    _events.emit(UiEvent.ShowMessage(message))
                    refresh()
                }
                .onFailure {
                    _events.emit(UiEvent.ShowMessage(it.message ?: "Не удалось обновить приглашение"))
                }
        }
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
