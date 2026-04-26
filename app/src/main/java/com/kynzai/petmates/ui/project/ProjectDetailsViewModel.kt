package com.kynzai.petmates.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.domain.models.ProjectStatus
import com.kynzai.domain.repositories.ProjectRepository
import com.kynzai.domain.repositories.UserRepository
import com.kynzai.petmates.session.SessionManager
import com.kynzai.petmates.ui.common.ScreenState
import com.kynzai.petmates.ui.common.UiEvent
import com.kynzai.petmates.ui.mappers.MemberUi
import com.kynzai.petmates.ui.mappers.ProjectUi
import com.kynzai.petmates.ui.mappers.UserUi
import com.kynzai.petmates.ui.mappers.VacancyUi
import com.kynzai.petmates.ui.mappers.toUi
import com.kynzai.petmates.ui.mappers.toUuidOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProjectDetailsUiState(
    val project: ProjectUi,
    val owner: UserUi,
    val members: List<MemberUi>,
    val vacancies: List<VacancyUi>,
    val isOwner: Boolean,
    val isAuthorized: Boolean,
)

@HiltViewModel
class ProjectDetailsViewModel @Inject constructor(
    private val projects: ProjectRepository,
    private val users: UserRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow<ScreenState<ProjectDetailsUiState>>(ScreenState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    private var projectId: String? = null

    fun load(id: String?) {
        projectId = id
        val uuid = id?.toUuidOrNull()
        if (uuid == null) {
            _state.value = ScreenState.Error("Некорректный ID проекта")
            return
        }

        viewModelScope.launch {
            _state.value = ScreenState.Loading
            val project = projects.getProjectById(uuid).getOrElse {
                _state.value = ScreenState.Error(it.message ?: "Проект не найден")
                return@launch
            }
            val owner = users.getUserById(project.ownerId).getOrElse {
                _state.value = ScreenState.Error(it.message ?: "Автор проекта не найден")
                return@launch
            }
            val members = projects.getProjectMembers(uuid).getOrDefault(emptyList())
            val vacancies = projects.getProjectVacancies(uuid).getOrDefault(emptyList())
            val me = sessionManager.state.value.currentUserId

            _state.value = ScreenState.Content(
                ProjectDetailsUiState(
                    project = project.toUi(),
                    owner = owner.toUi(),
                    members = members.map { it.toUi() },
                    vacancies = vacancies.map { it.toUi() },
                    isOwner = me == project.ownerId,
                    isAuthorized = me != null,
                )
            )
        }
    }

    fun retry() = load(projectId)

    fun rateProject() {
        val current = (_state.value as? ScreenState.Content)?.value ?: return
        if (!current.isAuthorized) {
            emitAuthRequired()
            return
        }
        if (current.isOwner) {
            emitMessage("Автор не может оценивать свой проект")
            return
        }

        viewModelScope.launch {
            projects.rateProject(current.project.id.toUuidOrNull()!!)
                .onSuccess {
                    emitMessage("Оценка засчитана")
                    retry()
                }
                .onFailure { emitMessage(it.message ?: "Не удалось оценить проект") }
        }
    }

    fun updateProject(
        name: String,
        shortDescription: String,
        fullDescription: String?,
        status: ProjectStatus,
    ) {
        val id = projectId?.toUuidOrNull() ?: return
        viewModelScope.launch {
            projects.updateProject(id, name, shortDescription, fullDescription, status)
                .onSuccess {
                    _events.emit(UiEvent.Saved)
                    retry()
                }
                .onFailure { emitMessage(it.message ?: "Не удалось сохранить проект") }
        }
    }

    fun requireAuth() = emitAuthRequired()

    private fun emitAuthRequired() {
        viewModelScope.launch { _events.emit(UiEvent.AuthRequired) }
    }

    private fun emitMessage(text: String) {
        viewModelScope.launch { _events.emit(UiEvent.ShowMessage(text)) }
    }
}
