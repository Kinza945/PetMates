package com.kynzai.petmates.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.petmates.session.SessionManager
import com.kynzai.petmates.ui.common.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val responseNotifications: Boolean = true,
    val inviteNotifications: Boolean = true,
    val projectNotifications: Boolean = true,
    val isBusy: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    fun setResponseNotifications(enabled: Boolean) =
        _state.update { it.copy(responseNotifications = enabled) }

    fun setInviteNotifications(enabled: Boolean) =
        _state.update { it.copy(inviteNotifications = enabled) }

    fun setProjectNotifications(enabled: Boolean) =
        _state.update { it.copy(projectNotifications = enabled) }

    fun changeEmail() {
        emitMessage("Смена почты пока работает как mock-сценарий")
    }

    fun changePassword() {
        emitMessage("Смена пароля пока работает как mock-сценарий")
    }

    fun deleteAccount() {
        emitMessage("Удаление аккаунта пока не подключено к серверу")
    }

    fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            sessionManager.logout()
                .onSuccess {
                    _events.emit(UiEvent.AuthRequired)
                }
                .onFailure {
                    _events.emit(UiEvent.ShowMessage(it.message ?: "Не удалось выйти из аккаунта"))
                }
            _state.update { it.copy(isBusy = false) }
        }
    }

    private fun emitMessage(text: String) {
        viewModelScope.launch {
            _events.emit(UiEvent.ShowMessage(text))
        }
    }
}
