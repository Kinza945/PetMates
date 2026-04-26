package com.kynzai.domain.repositories

import com.kynzai.domain.models.User
import com.kynzai.domain.models.UserProfileUpdate
import com.kynzai.domain.common.Page
import com.kynzai.domain.common.PageToken
import com.kynzai.domain.common.SortOrder
import com.kynzai.domain.common.UserSearchQuery
import java.util.UUID

interface UserRepository {
    suspend fun getUserById(userId: UUID): Result<User>
    suspend fun getUserByNickname(nickname: String): Result<User>
    suspend fun searchUsers(query: String): Result<List<User>>

    suspend fun updateProfile(update: UserProfileUpdate): Result<User> =
        Result.failure(UnsupportedOperationException("updateProfile is not implemented"))

    /**
     * Расширенный поиск с пагинацией/фильтрами. По умолчанию строится на searchUsers(String).
     */
    suspend fun searchUsers(query: UserSearchQuery): Result<Page<User>> =
        searchUsers(query.q.orEmpty()).mapCatching { all ->
            val filtered = all.asSequence()
                .filter { u -> query.country == null || u.country.equals(query.country, ignoreCase = true) }
                .filter { u -> query.city == null || u.city.equals(query.city, ignoreCase = true) }
                .filter { u ->
                    if (query.skills.isEmpty()) true
                    else query.skills.any { s ->
                        u.hardSkills.any { it.contains(s, ignoreCase = true) } ||
                            u.softSkills.any { it.contains(s, ignoreCase = true) }
                    }
                }
                .toList()

            val sorted = when (query.sort) {
                SortOrder.LAST_ONLINE_DESC -> filtered.sortedByDescending { it.lastOnlineAt }
                else -> filtered
            }

            val offset = (query.page.token as? PageToken.Offset)?.value ?: 0
            val limit = query.page.limit.coerceAtLeast(1)
            val pageItems = sorted.drop(offset).take(limit)
            val nextOffset = offset + pageItems.size
            val nextToken = if (nextOffset >= sorted.size) null else PageToken.Offset(nextOffset)
            Page(items = pageItems, nextToken = nextToken)
        }
}
