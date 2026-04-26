package com.kynzai.petmates.ui.common

sealed interface UiEvent {
    data object AuthRequired : UiEvent
    data object NavigateBack : UiEvent
    data object Saved : UiEvent
    data class ShowMessage(val text: String) : UiEvent
    data class Created(val id: String) : UiEvent
    data class NavigateToProject(val projectId: String) : UiEvent
    data class NavigateToInvite(val projectId: String) : UiEvent
}

sealed interface ScreenState<out T> {
    data object Loading : ScreenState<Nothing>
    data object Unauthorized : ScreenState<Nothing>
    data class Content<T>(val value: T) : ScreenState<T>
    data class Error(val message: String) : ScreenState<Nothing>
}
