package com.kynzai.petmates.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.domain.common.AppResult
import com.kynzai.domain.common.DomainError
import com.kynzai.domain.common.LoadState
import com.kynzai.domain.common.NotificationsQuery
import com.kynzai.domain.common.PageRequest
import com.kynzai.domain.common.toLoadState
import com.kynzai.domain.models.Notification
import com.kynzai.domain.models.NotificationCategory
import com.kynzai.domain.repositories.NotificationRepository
import com.kynzai.domain.usecases.GetNotificationsUseCase
import com.kynzai.petmates.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotifications: GetNotificationsUseCase,
    private val repo: NotificationRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<List<NotificationUiModel>>>(LoadState.Loading)
    val state = _state.asStateFlow()

    private val zone: ZoneId = ZoneId.systemDefault()
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = LoadState.Loading
            val me = sessionManager.state.value.currentUserId
            if (me == null) {
                // Уведомления — приватные данные, поэтому для гостя явно возвращаем Unauthorized.
                _state.value = LoadState.Error(DomainError.Unauthorized)
                return@launch
            }

            when (val res = getNotifications(NotificationsQuery(userId = me, page = PageRequest(limit = 100)))) {
                is AppResult.Success -> _state.value = LoadState.Data(res.data.items.map { it.toUi() })
                is AppResult.Error -> _state.value = res.toLoadState()
            }
        }
    }

    fun markAllRead() {
        val current = (_state.value as? LoadState.Data)?.value.orEmpty()
        viewModelScope.launch {
            // Best-effort: отмечаем каждое уведомление и затем перезагружаем состояние.
            current.filter { it.isUnread }.forEach { item ->
                runCatching { repo.markAsRead(UUID.fromString(item.id)) }
            }
            refresh()
        }
    }

    fun markRead(id: UUID) {
        viewModelScope.launch {
            repo.markAsRead(id)
            refresh()
        }
    }

    fun delete(id: UUID) {
        viewModelScope.launch {
            repo.deleteNotification(id)
            refresh()
        }
    }

    private fun Notification.toUi(): NotificationUiModel {
        val type = when (category) {
            NotificationCategory.RESPONSE ->
                when {
                    eventType.contains("reject", ignoreCase = true) -> NotificationType.Danger
                    eventType.contains("accept", ignoreCase = true) -> NotificationType.Success
                    else -> NotificationType.Info
                }

            NotificationCategory.INVITATION -> NotificationType.Info
            NotificationCategory.PROJECT -> NotificationType.Info
        }

        val date = createdAt?.atZone(zone)?.format(formatter) ?: ""
        val text = buildString {
            val project = contextData["project_name"]?.toString()
            val vacancy = contextData["vacancy_title"]?.toString()
            val message = contextData["message"]?.toString()
            val fallback = contextData["text"]?.toString()

            // Mock может дать готовый текст, а real API — структурированный context_data.
            if (!fallback.isNullOrBlank()) {
                append(fallback)
                return@buildString
            }

            when (category) {
                NotificationCategory.RESPONSE -> {
                    when {
                        eventType.contains("rejected", ignoreCase = true) -> append("Ваш отклик был отклонен")
                        eventType.contains("accepted", ignoreCase = true) -> append("Ваш отклик был принят")
                        else -> append("Новый отклик по вакансии")
                    }
                    if (!vacancy.isNullOrBlank()) append(" «$vacancy»")
                    if (!project.isNullOrBlank()) append(" в проект «$project»")
                }

                NotificationCategory.INVITATION -> {
                    append("Приглашение в проект")
                    if (!project.isNullOrBlank()) append(" «$project»")
                    if (!message.isNullOrBlank()) append(". Сообщение: $message")
                }

                NotificationCategory.PROJECT -> {
                    append("Обновление по проекту")
                    if (!project.isNullOrBlank()) append(" «$project»")
                }
            }
        }

        return NotificationUiModel(
            id = notificationId.toString(),
            type = type,
            text = text,
            date = date,
            isUnread = !isRead,
        )
    }
}
