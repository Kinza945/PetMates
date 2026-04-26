package com.kynzai.petmates.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.domain.models.ProjectStatus
import com.kynzai.domain.repositories.ProjectRepository
import com.kynzai.petmates.session.SessionManager
import com.kynzai.petmates.ui.common.ScreenState
import com.kynzai.petmates.ui.common.UiEvent
import com.kynzai.petmates.ui.mappers.toUuidOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProjectForm(
    val id: String = "",
    val name: String = "",
    val shortDescription: String = "",
    val fullDescription: String = "",
    val status: ProjectStatus = ProjectStatus.IN_PROGRESS,
    val nameError: String? = null,
    val shortDescriptionError: String? = null,
) {
    val canSave: Boolean get() = name.isNotBlank() && shortDescription.isNotBlank()
}

@HiltViewModel
class EditProjectViewModel @Inject constructor(
    private val projects: ProjectRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow<ScreenState<EditProjectForm>>(ScreenState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    fun load(projectId: String?) {
        val id = projectId?.toUuidOrNull()
        if (id == null) {
            _state.value = ScreenState.Error("Некорректный ID проекта")
            return
        }
        if (sessionManager.state.value.currentUserId == null) {
            _state.value = ScreenState.Unauthorized
            return
        }

        viewModelScope.launch {
            _state.value = ScreenState.Loading
            val project = projects.getProjectById(id).getOrElse {
                _state.value = ScreenState.Error(it.message ?: "Проект не найден")
                return@launch
            }
            val me = sessionManager.state.value.currentUserId
            if (project.ownerId != me) {
                _state.value = ScreenState.Error("Редактировать проект может только владелец")
                return@launch
            }
            _state.value = ScreenState.Content(
                EditProjectForm(
                    id = project.projectId.toString(),
                    name = project.name,
                    shortDescription = project.shortDescription,
                    fullDescription = project.fullDescription.orEmpty(),
                    status = project.status,
                )
            )
        }
    }

    fun onNameChanged(value: String) = updateForm { copy(name = value, nameError = null) }
    fun onShortDescriptionChanged(value: String) = updateForm { copy(shortDescription = value, shortDescriptionError = null) }
    fun onFullDescriptionChanged(value: String) = updateForm { copy(fullDescription = value) }
    fun onStatusChanged(value: ProjectStatus) = updateForm { copy(status = value) }

    fun save() {
        val form = (_state.value as? ScreenState.Content)?.value ?: return
        val validated = form.copy(
            nameError = if (form.name.isBlank()) "Введите название проекта" else null,
            shortDescriptionError = if (form.shortDescription.isBlank()) "Введите краткое описание" else null,
        )
        _state.value = ScreenState.Content(validated)
        if (!validated.canSave) return

        viewModelScope.launch {
            projects.updateProject(
                projectId = form.id.toUuidOrNull()!!,
                name = form.name,
                shortDescription = form.shortDescription,
                fullDescription = form.fullDescription.ifBlank { null },
                status = form.status,
            ).onSuccess {
                _events.emit(UiEvent.Saved)
            }.onFailure {
                _events.emit(UiEvent.ShowMessage(it.message ?: "Не удалось сохранить проект"))
            }
        }
    }

    private fun updateForm(block: EditProjectForm.() -> EditProjectForm) {
        _state.update { current ->
            val form = (current as? ScreenState.Content)?.value ?: return@update current
            ScreenState.Content(form.block())
        }
    }
}
