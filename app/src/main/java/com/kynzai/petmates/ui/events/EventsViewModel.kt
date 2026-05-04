package com.kynzai.petmates.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.domain.common.AppResult
import com.kynzai.domain.common.LoadState
import com.kynzai.domain.common.PageRequest
import com.kynzai.domain.common.ProjectFeedQuery
import com.kynzai.domain.common.toLoadState
import com.kynzai.domain.models.Project
import com.kynzai.domain.models.User
import com.kynzai.domain.models.Vacancy
import com.kynzai.domain.repositories.ResponseRepository
import com.kynzai.domain.repositories.UserRepository
import com.kynzai.domain.repositories.VacancyRepository
import com.kynzai.domain.usecases.GetFeedProjectsUseCase
import com.kynzai.petmates.session.SessionManager
import com.kynzai.petmates.ui.common.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class FeedProjectUi(
    val projectId: UUID,
    val vacancyId: UUID?,
    val openVacancies: List<FeedVacancyUi>,
    val role: String,
    val ratingCount: Int,
    val averageRating: Double?,
    val name: String,
    val description: String,
    val tags: List<String>,
    val authorName: String,
    val isOnline: Boolean,
    val hasPendingResponse: Boolean,
)

data class FeedVacancyUi(
    val vacancyId: UUID,
    val title: String,
    val tags: List<String>,
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val getFeedProjects: GetFeedProjectsUseCase,
    private val vacancies: VacancyRepository,
    private val users: UserRepository,
    private val responses: ResponseRepository,
    private val projects: com.kynzai.domain.repositories.ProjectRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<List<FeedProjectUi>>>(LoadState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    private var lastQuery: String = ""

    fun load(query: String) {
        lastQuery = query
        viewModelScope.launch {
            _state.value = LoadState.Loading
            val res = getFeedProjects(
                ProjectFeedQuery(
                    q = query.ifBlank { null },
                    page = PageRequest(limit = 50),
                )
            )

            when (res) {
                is AppResult.Success -> {
                    val items = res.data.items.map { project ->
                        project.toFeedUi()
                    }
                    _state.value = LoadState.Data(items)
                }

                is AppResult.Error -> _state.value = res.toLoadState()
            }
        }
    }

    fun respondToVacancy(vacancyId: UUID) {
        viewModelScope.launch {
            if (sessionManager.state.value.currentUserId == null) {
                _events.emit(UiEvent.AuthRequired)
                return@launch
            }
            responses.createResponse(vacancyId)
                .onSuccess {
                    _events.emit(UiEvent.ShowMessage("Отклик отправлен"))
                    load(lastQuery)
                }
                .onFailure {
                    _events.emit(UiEvent.ShowMessage(it.message ?: "Не удалось отправить отклик"))
                }
        }
    }

    private suspend fun Project.toFeedUi(): FeedProjectUi {
        val vs = vacancies.getVacanciesByProject(projectId).getOrDefault(emptyList())
        val picked = vs.firstOpenOrNull() ?: vs.firstOrNull()
        val open = vs.filter { it.isOpen }
        val me = sessionManager.state.value.currentUserId
        val hasPending = me != null && vs.any { vacancy ->
            responses.getResponsesByVacancy(vacancy.vacancyId)
                .getOrDefault(emptyList())
                .any { it.userId == me && it.status == com.kynzai.domain.models.ResponseStatus.PENDING }
        }

        val author = users.getUserById(ownerId).getOrNull()
        val authorName = author?.nickname ?: "Unknown"
        val isOnline = author?.isOnlineNow() ?: false
        val ratingItems = projects.getProjectRatings(projectId).getOrDefault(emptyList())
        val averageRating = ratingItems.takeIf { it.isNotEmpty() }?.map { it.score }?.average()

        return FeedProjectUi(
            projectId = projectId,
            vacancyId = picked?.vacancyId,
            openVacancies = open.map { FeedVacancyUi(it.vacancyId, it.title, it.requiredTags) },
            role = picked?.title ?: "Участник команды",
            ratingCount = ratingCount,
            averageRating = averageRating,
            name = name,
            description = shortDescription,
            tags = picked?.requiredTags?.take(6) ?: emptyList(),
            authorName = authorName,
            isOnline = isOnline,
            hasPendingResponse = hasPending,
        )
    }

    private fun List<Vacancy>.firstOpenOrNull(): Vacancy? =
        firstOrNull { it.isOpen }

    private fun User.isOnlineNow(): Boolean {
        val last = lastOnlineAt ?: return false
        // "Онлайн" если был в сети в последние 30 минут.
        return Duration.between(last, Instant.now()).abs() <= Duration.ofMinutes(30)
    }
}
