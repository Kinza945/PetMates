package com.kynzai.petmates.ui.vacancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.domain.repositories.VacancyRepository
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

data class EditVacancyForm(
    val id: String = "",
    val title: String = "",
    val role: String = "",
    val description: String = "",
    val requiredTagsRaw: String = "",
    val isOpen: Boolean = true,
    val titleError: String? = null,
    val roleError: String? = null,
    val descriptionError: String? = null,
) {
    val canSave: Boolean get() = title.isNotBlank() && role.isNotBlank() && description.isNotBlank()
}

@HiltViewModel
class EditVacancyViewModel @Inject constructor(
    private val vacancies: VacancyRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow<ScreenState<EditVacancyForm>>(ScreenState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    fun load(vacancyId: String?) {
        val id = vacancyId?.toUuidOrNull()
        if (id == null) {
            _state.value = ScreenState.Error("Некорректный ID вакансии")
            return
        }
        if (sessionManager.state.value.currentUserId == null) {
            _state.value = ScreenState.Unauthorized
            return
        }

        viewModelScope.launch {
            _state.value = ScreenState.Loading
            val vacancy = vacancies.getVacancyById(id).getOrElse {
                _state.value = ScreenState.Error(it.message ?: "Вакансия не найдена")
                return@launch
            }
            _state.value = ScreenState.Content(
                EditVacancyForm(
                    id = vacancy.vacancyId.toString(),
                    title = vacancy.title,
                    role = vacancy.role,
                    description = vacancy.description,
                    requiredTagsRaw = vacancy.requiredTags.joinToString(" "),
                    isOpen = vacancy.isOpen,
                )
            )
        }
    }

    fun onTitleChanged(v: String) = updateForm { copy(title = v, titleError = null) }
    fun onRoleChanged(v: String) = updateForm { copy(role = v, roleError = null) }
    fun onDescriptionChanged(v: String) = updateForm { copy(description = v, descriptionError = null) }
    fun onRequiredTagsChanged(v: String) = updateForm { copy(requiredTagsRaw = v) }
    fun onIsOpenChanged(v: Boolean) = updateForm { copy(isOpen = v) }

    fun save() {
        val form = (_state.value as? ScreenState.Content)?.value ?: return
        val validated = form.copy(
            titleError = if (form.title.isBlank()) "Введите название" else null,
            roleError = if (form.role.isBlank()) "Введите роль" else null,
            descriptionError = if (form.description.isBlank()) "Введите описание" else null,
        )
        _state.value = ScreenState.Content(validated)
        if (!validated.canSave) return

        viewModelScope.launch {
            vacancies.updateVacancy(
                vacancyId = form.id.toUuidOrNull()!!,
                title = form.title,
                role = form.role,
                description = form.description,
                requiredTags = form.requiredTagsRaw.toTags(),
                isOpen = form.isOpen,
            ).onSuccess {
                _events.emit(UiEvent.Saved)
            }.onFailure {
                _events.emit(UiEvent.ShowMessage(it.message ?: "Не удалось сохранить вакансию"))
            }
        }
    }

    private fun updateForm(block: EditVacancyForm.() -> EditVacancyForm) {
        _state.update { current ->
            val form = (current as? ScreenState.Content)?.value ?: return@update current
            ScreenState.Content(form.block())
        }
    }
}

fun String.toTags(): List<String> =
    split(",", " ")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .map { if (it.startsWith("#")) it else "#$it" }
        .distinct()
