package com.kynzai.petmates.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.domain.repositories.ProjectRepository
import com.kynzai.domain.repositories.UserRepository
import com.kynzai.petmates.session.SessionManager
import com.kynzai.petmates.ui.common.ScreenState
import com.kynzai.petmates.ui.common.UiEvent
import com.kynzai.petmates.ui.mappers.ProjectUi
import com.kynzai.petmates.ui.mappers.UserUi
import com.kynzai.petmates.ui.mappers.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val user: UserUi,
    val myProjects: List<ProjectUi>,
    val isAuthorized: Boolean,
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val users: UserRepository,
    private val projects: ProjectRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow<ScreenState<UserProfileUiState>>(ScreenState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    private var nickname: String? = null
    private val cacheTtlMs = 60_000L
    private var lastLoadedAtMs: Long = 0L
    private var lastLoadedNickname: String? = null

    fun load(nickname: String?, force: Boolean = false) {
        this.nickname = nickname
        val nick = nickname?.trim().orEmpty()
        if (nick.isBlank()) {
            _state.value = ScreenState.Error("Некорректный nickname")
            return
        }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val hasContent = _state.value is ScreenState.Content
            if (!force && hasContent && lastLoadedNickname == nick && now - lastLoadedAtMs <= cacheTtlMs) {
                return@launch
            }
            if (!hasContent) {
                _state.value = ScreenState.Loading
            }
            val user = users.getUserByNickname(nick).getOrElse {
                _state.value = ScreenState.Error(it.message ?: "Пользователь не найден")
                return@launch
            }
            val me = sessionManager.state.value.currentUserId
            val myProjects = if (me != null) {
                projects.getAllProjects().getOrDefault(emptyList()).filter { it.ownerId == me }.map { it.toUi() }
            } else {
                emptyList()
            }
            _state.value = ScreenState.Content(
                UserProfileUiState(
                    user = user.toUi(),
                    myProjects = myProjects,
                    isAuthorized = me != null,
                )
            )
            lastLoadedAtMs = now
            lastLoadedNickname = nick
        }
    }

    fun retry() = load(nickname, force = true)

    fun onInviteClick() {
        val current = (_state.value as? ScreenState.Content)?.value ?: return
        if (!current.isAuthorized) {
            viewModelScope.launch { _events.emit(UiEvent.AuthRequired) }
            return
        }
        if (current.myProjects.isEmpty()) {
            viewModelScope.launch { _events.emit(UiEvent.ShowMessage("Сначала создайте проект")) }
            return
        }
        if (current.myProjects.size == 1) {
            viewModelScope.launch { _events.emit(UiEvent.NavigateToInvite(current.myProjects.first().id)) }
        }
    }
}
