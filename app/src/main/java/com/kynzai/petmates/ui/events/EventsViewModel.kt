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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class FeedProjectUi(
    val projectId: UUID,
    val vacancyId: UUID?,
    val role: String,
    val ratingCount: Int,
    val name: String,
    val description: String,
    val tags: List<String>,
    val authorName: String,
    val isOnline: Boolean,
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val getFeedProjects: GetFeedProjectsUseCase,
    private val vacancies: VacancyRepository,
    private val users: UserRepository,
    private val responses: ResponseRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<List<FeedProjectUi>>>(LoadState.Loading)
    val state = _state.asStateFlow()

    fun load(query: String) {
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
            // Best-effort: действие выполняется, а отдельные snackbar-сообщения можно добавить позже.
            responses.createResponse(vacancyId)
        }
    }

    private suspend fun Project.toFeedUi(): FeedProjectUi {
        val vs = vacancies.getVacanciesByProject(projectId).getOrDefault(emptyList())
        val picked = vs.firstOpenOrNull() ?: vs.firstOrNull()

        val author = users.getUserById(ownerId).getOrNull()
        val authorName = author?.nickname ?: "Unknown"
        val isOnline = author?.isOnlineNow() ?: false

        return FeedProjectUi(
            projectId = projectId,
            vacancyId = picked?.vacancyId,
            role = picked?.title ?: "Участник команды",
            ratingCount = ratingCount,
            name = name,
            description = shortDescription,
            tags = picked?.requiredTags?.take(6) ?: emptyList(),
            authorName = authorName,
            isOnline = isOnline,
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
