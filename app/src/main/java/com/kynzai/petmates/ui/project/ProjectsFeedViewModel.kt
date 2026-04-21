package com.kynzai.petmates.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.domain.models.Project // Убедитесь, что этот импорт верный!
import com.kynzai.domain.repositories.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// Состояния экрана
sealed class ProjectsUiState {
    object Loading : ProjectsUiState()
    data class Success(val projects: List<Project>) : ProjectsUiState()
    data class Error(val message: String) : ProjectsUiState()
}

@HiltViewModel
class ProjectsFeedViewModel @Inject constructor(
    private val repository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectsUiState>(ProjectsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            _uiState.value = ProjectsUiState.Loading
            val result = repository.getAllProjects()
            result.onSuccess { projectsList ->
                _uiState.value = ProjectsUiState.Success(projectsList)
            }.onFailure { error ->
                // Пока Supabase может быть не настроен, показываем демо-ленту,
                // чтобы UI можно было нормально верстать.
                val msg = error.localizedMessage ?: "Ошибка"
                if (msg.contains("SUPABASE_URL is empty", ignoreCase = true)) {
                    _uiState.value = ProjectsUiState.Success(demoProjects())
                } else {
                    _uiState.value = ProjectsUiState.Error(msg)
                }
            }
        }
    }

    private fun demoProjects(): List<Project> =
        listOf(
            Project(
                projectId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                ownerId = UUID.fromString("00000000-0000-0000-0000-000000000011"),
                name = "Приложение Contacts",
                shortDescription = "Нашему проекту требуется разработчик, который сможет сверстать сайт на React...",
                ratingCount = 4,
            ),
            Project(
                projectId = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                ownerId = UUID.fromString("00000000-0000-0000-0000-000000000012"),
                name = "Task Tracker",
                shortDescription = "Ищем фронтенд-разработчика для задачника с аналитикой и уведомлениями.",
                ratingCount = 12,
            ),
            Project(
                projectId = UUID.fromString("00000000-0000-0000-0000-000000000003"),
                ownerId = UUID.fromString("00000000-0000-0000-0000-000000000013"),
                name = "PetMates",
                shortDescription = "Платформа для поиска команды под pet-проекты. Нужны React + hooks.",
                ratingCount = 28,
            ),
        )
}
