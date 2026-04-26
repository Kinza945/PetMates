package com.kynzai.petmates.ui.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.domain.common.AppResult
import com.kynzai.domain.common.DomainError
import com.kynzai.domain.common.LoadState
import com.kynzai.domain.common.toDomainError
import com.kynzai.domain.common.toLoadState
import com.kynzai.domain.models.ResponseStatus
import com.kynzai.domain.repositories.ProjectRepository
import com.kynzai.domain.repositories.ResponseRepository
import com.kynzai.domain.repositories.UserRepository
import com.kynzai.domain.usecases.UpdateResponseStatusUseCase
import com.kynzai.petmates.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

data class IncomingRequestUi(
    val responseId: UUID,
    val fromName: String,
    val date: String,
    val text: String,
)

data class OutgoingRequestUi(
    val responseId: UUID,
    val projectName: String,
    val date: String,
    val text: String,
    val status: OutgoingStatus,
)

data class RequestsUiModel(
    val incoming: List<IncomingRequestUi>,
    val outgoing: List<OutgoingRequestUi>,
)

@HiltViewModel
class RequestsViewModel @Inject constructor(
    private val projects: ProjectRepository,
    private val responses: ResponseRepository,
    private val users: UserRepository,
    private val updateResponseStatus: UpdateResponseStatusUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<RequestsUiModel>>(LoadState.Loading)
    val state = _state.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val zone: ZoneId = ZoneId.systemDefault()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = LoadState.Loading
            val me = sessionManager.state.value.currentUserId
            if (me == null) {
                // Гость должен увидеть CTA авторизации, а не пустой список заявок.
                _state.value = LoadState.Error(DomainError.Unauthorized)
                return@launch
            }

            val allProjects = projects.getAllProjects()
            val allProjectsList = allProjects.getOrElse {
                _state.value = LoadState.Error(it.toDomainError())
                return@launch
            }

            val incoming = mutableListOf<IncomingRequestUi>()
            val outgoing = mutableListOf<OutgoingRequestUi>()

            // Входящие = pending-отклики других пользователей на вакансии моих проектов.
            val myProjects = allProjectsList.filter { it.ownerId == me }
            for (p in myProjects) {
                val vs = projects.getProjectVacancies(p.projectId).getOrDefault(emptyList())
                for (v in vs) {
                    val rs = responses.getResponsesByVacancy(v.vacancyId).getOrDefault(emptyList())
                    rs.filter { it.status == ResponseStatus.PENDING && it.userId != me }.forEach { r ->
                        val from = users.getUserById(r.userId).getOrNull()?.nickname ?: "Unknown"
                        val date = r.createdAt?.atZone(zone)?.format(dateFormatter) ?: ""
                        incoming.add(
                            IncomingRequestUi(
                                responseId = r.responseId,
                                fromName = from,
                                date = date,
                                text = "Отклик на роль «${v.title}» в проект «${p.name}»",
                            )
                        )
                    }
                }
            }

            // Исходящие = мои отклики на любые вакансии, независимо от статуса.
            for (p in allProjectsList) {
                val vs = projects.getProjectVacancies(p.projectId).getOrDefault(emptyList())
                for (v in vs) {
                    val rs = responses.getResponsesByVacancy(v.vacancyId).getOrDefault(emptyList())
                    rs.filter { it.userId == me }.forEach { r ->
                        val date = r.createdAt?.atZone(zone)?.format(dateFormatter) ?: ""
                        outgoing.add(
                            OutgoingRequestUi(
                                responseId = r.responseId,
                                projectName = p.name,
                                date = date,
                                text = "Ваш отклик на роль «${v.title}»",
                                status = when (r.status) {
                                    ResponseStatus.PENDING -> OutgoingStatus.Pending
                                    ResponseStatus.ACCEPTED -> OutgoingStatus.Accepted
                                    ResponseStatus.REJECTED -> OutgoingStatus.Rejected
                                },
                            )
                        )
                    }
                }
            }

            _state.value = LoadState.Data(
                RequestsUiModel(
                    incoming = incoming.sortedByDescending { it.date },
                    outgoing = outgoing.sortedByDescending { it.date },
                )
            )
        }
    }

    fun accept(responseId: UUID) = update(responseId, ResponseStatus.ACCEPTED)
    fun reject(responseId: UUID) = update(responseId, ResponseStatus.REJECTED)

    private fun update(responseId: UUID, status: ResponseStatus) {
        viewModelScope.launch {
            when (val res = updateResponseStatus(responseId, status)) {
                is AppResult.Success -> refresh()
                is AppResult.Error -> _state.value = res.toLoadState()
            }
        }
    }
}
