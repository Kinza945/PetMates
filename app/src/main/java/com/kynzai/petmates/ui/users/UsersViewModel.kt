package com.kynzai.petmates.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.domain.common.AppResult
import com.kynzai.domain.common.LoadState
import com.kynzai.domain.common.PageRequest
import com.kynzai.domain.common.UserSearchQuery
import com.kynzai.domain.common.toLoadState
import com.kynzai.domain.models.User
import com.kynzai.domain.usecases.SearchUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserCardUi(
    val nickname: String,
    val role: String,
    val skills: List<String>,
)

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val searchUsers: SearchUsersUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<List<UserCardUi>>>(LoadState.Loading)
    val state = _state.asStateFlow()

    fun load(query: String) {
        viewModelScope.launch {
            _state.value = LoadState.Loading
            val res = searchUsers(
                UserSearchQuery(
                    q = query.ifBlank { null },
                    page = PageRequest(limit = 50),
                )
            )

            when (res) {
                is AppResult.Success -> _state.value = LoadState.Data(res.data.items.map { it.toCard() })
                is AppResult.Error -> _state.value = res.toLoadState()
            }
        }
    }

    private fun User.toCard(): UserCardUi =
        UserCardUi(
            nickname = nickname,
            role = profileRole ?: "Участник",
            skills = (hardSkills + softSkills)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .take(6),
        )
}

