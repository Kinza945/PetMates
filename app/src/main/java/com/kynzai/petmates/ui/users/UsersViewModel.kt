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
    val avatarUrl: String?,
)

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val searchUsers: SearchUsersUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<List<UserCardUi>>>(LoadState.Loading)
    val state = _state.asStateFlow()

    private data class CacheEntry(
        val value: List<UserCardUi>,
        val loadedAtMs: Long,
    )

    private val cache = mutableMapOf<String, CacheEntry>()
    private val cacheTtlMs = 60_000L

    fun load(query: String, force: Boolean = false) {
        viewModelScope.launch {
            val key = query.trim()
            val now = System.currentTimeMillis()
            val cached = cache[key]

            if (!force && cached != null && now - cached.loadedAtMs <= cacheTtlMs) {
                _state.value = LoadState.Data(cached.value)
                return@launch
            }

            if (cached == null) {
                _state.value = LoadState.Loading
            } else {
                _state.value = LoadState.Data(cached.value)
            }

            val res = searchUsers(
                UserSearchQuery(
                    q = key.ifBlank { null },
                    page = PageRequest(limit = 50),
                )
            )

            when (res) {
                is AppResult.Success -> {
                    val cards = res.data.items.map { it.toCard() }
                    cache[key] = CacheEntry(value = cards, loadedAtMs = now)
                    _state.value = LoadState.Data(cards)
                }

                is AppResult.Error -> {
                    // Если есть кеш — не "проваливаем" UI в ошибку, просто оставляем старые данные.
                    if (cached == null) {
                        _state.value = res.toLoadState()
                    }
                }
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
            avatarUrl = avatarUrl,
        )
}

