package com.kynzai.petmates.ui.invite

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.domain.common.LoadState
import com.kynzai.domain.common.PageRequest
import com.kynzai.domain.common.UserSearchQuery
import com.kynzai.domain.common.toLoadState
import com.kynzai.domain.models.Invite
import com.kynzai.domain.models.User
import com.kynzai.domain.usecases.CreateInviteUseCase
import com.kynzai.domain.usecases.SearchUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class InviteUserViewModel @Inject constructor(
    private val searchUsers: SearchUsersUseCase,
    private val createInvite: CreateInviteUseCase,
) : ViewModel() {
    var query by mutableStateOf("")
        private set

    var selectedUserId: UUID? by mutableStateOf(null)
        private set

    var role by mutableStateOf("")
        private set

    var message by mutableStateOf("")
        private set

    var usersState: LoadState<List<User>> by mutableStateOf(LoadState.Idle)
        private set

    var inviteState: LoadState<Invite> by mutableStateOf(LoadState.Idle)
        private set

    val isValid: Boolean
        get() = selectedUserId != null && role.isNotBlank()

    fun updateQuery(v: String) {
        query = v
    }

    fun updateRole(v: String) {
        role = v
    }

    fun updateMessage(v: String) {
        message = v
    }

    fun selectUser(id: UUID) {
        selectedUserId = id
    }

    fun search() {
        usersState = LoadState.Loading
        viewModelScope.launch {
            val res = searchUsers(
                UserSearchQuery(
                    q = query.ifBlank { null },
                    page = PageRequest(limit = 50),
                )
            )
            usersState = when (res) {
                is com.kynzai.domain.common.AppResult.Success -> LoadState.Data(res.data.items)
                is com.kynzai.domain.common.AppResult.Error -> LoadState.Error(res.error)
            }
        }
    }

    fun submit(projectId: UUID) {
        val userId = selectedUserId ?: return
        if (role.isBlank()) return
        if (inviteState is LoadState.Loading) return

        inviteState = LoadState.Loading
        viewModelScope.launch {
            val res = createInvite(
                projectId = projectId,
                userId = userId,
                role = role,
                message = message.ifBlank { null },
            )
            inviteState = res.toLoadState()
        }
    }

    fun consumeCreated() {
        if (inviteState is LoadState.Data) inviteState = LoadState.Idle
    }
}
