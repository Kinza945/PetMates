package com.kynzai.petmates.ui.vacancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.domain.models.ResponseStatus
import com.kynzai.domain.repositories.ProjectRepository
import com.kynzai.domain.repositories.ResponseRepository
import com.kynzai.domain.repositories.VacancyRepository
import com.kynzai.petmates.session.SessionManager
import com.kynzai.petmates.ui.common.ScreenState
import com.kynzai.petmates.ui.common.UiEvent
import com.kynzai.petmates.ui.mappers.ProjectUi
import com.kynzai.petmates.ui.mappers.ResponseUi
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

data class VacancyDetailsUiState(
    val vacancy: VacancyUi,
    val project: ProjectUi,
    val pendingResponse: ResponseUi?,
    val isAuthorized: Boolean,
)

@HiltViewModel
class VacancyDetailsViewModel @Inject constructor(
    private val vacancies: VacancyRepository,
    private val projects: ProjectRepository,
    private val responses: ResponseRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow<ScreenState<VacancyDetailsUiState>>(ScreenState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    private var vacancyId: String? = null

    fun load(id: String?) {
        vacancyId = id
        val uuid = id?.toUuidOrNull()
        if (uuid == null) {
            _state.value = ScreenState.Error("Некорректный ID вакансии")
            return
        }

        viewModelScope.launch {
            _state.value = ScreenState.Loading
            val vacancy = vacancies.getVacancyById(uuid).getOrElse {
                _state.value = ScreenState.Error(it.message ?: "Вакансия не найдена")
                return@launch
            }
            val project = projects.getProjectById(vacancy.projectId).getOrElse {
                _state.value = ScreenState.Error(it.message ?: "Проект не найден")
                return@launch
            }
            val me = sessionManager.state.value.currentUserId
            val pending = if (me != null) {
                responses.getResponsesByVacancy(uuid).getOrDefault(emptyList())
                    .firstOrNull { it.userId == me && it.status == ResponseStatus.PENDING }
            } else {
                null
            }
            _state.value = ScreenState.Content(
                VacancyDetailsUiState(
                    vacancy = vacancy.toUi(),
                    project = project.toUi(),
                    pendingResponse = pending?.toUi(),
                    isAuthorized = me != null,
                )
            )
        }
    }

    fun retry() = load(vacancyId)

    fun onRespondOrCancelClick() {
        val current = (_state.value as? ScreenState.Content)?.value ?: return
        if (!current.isAuthorized) {
            viewModelScope.launch { _events.emit(UiEvent.AuthRequired) }
            return
        }

        viewModelScope.launch {
            val pending = current.pendingResponse
            if (pending != null) {
                responses.cancelResponse(pending.id.toUuidOrNull()!!)
                    .onSuccess {
                        _events.emit(UiEvent.ShowMessage("Отклик отменён"))
                        retry()
                    }
                    .onFailure { _events.emit(UiEvent.ShowMessage(it.message ?: "Не удалось отменить отклик")) }
            } else {
                responses.createResponse(current.vacancy.id.toUuidOrNull()!!)
                    .onSuccess {
                        _events.emit(UiEvent.ShowMessage("Отклик отправлен"))
                        retry()
                    }
                    .onFailure { _events.emit(UiEvent.ShowMessage(it.message ?: "Не удалось отправить отклик")) }
            }
        }
    }
}
