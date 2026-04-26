package com.kynzai.domain.usecases

import com.kynzai.domain.common.AppResult
import com.kynzai.domain.common.Page
import com.kynzai.domain.common.UserSearchQuery
import com.kynzai.domain.common.toAppResult
import com.kynzai.domain.models.User
import com.kynzai.domain.repositories.UserRepository

class SearchUsersUseCase(
    private val users: UserRepository,
) {
    suspend operator fun invoke(query: UserSearchQuery): AppResult<Page<User>> =
        users.searchUsers(query).toAppResult()
}

