package com.kynzai.domain.common

import com.kynzai.domain.models.NotificationCategory
import com.kynzai.domain.models.ProjectStatus
import java.util.UUID

enum class SortOrder {
    CREATED_AT_DESC,
    CREATED_AT_ASC,
    LAST_ONLINE_DESC,
    RATING_DESC,
}

data class ProjectFeedQuery(
    val q: String? = null,
    val tags: List<String> = emptyList(),
    val status: ProjectStatus? = null,
    val sort: SortOrder = SortOrder.CREATED_AT_DESC,
    val page: PageRequest = PageRequest(),
)

data class UserSearchQuery(
    val q: String? = null,
    val country: String? = null,
    val city: String? = null,
    val skills: List<String> = emptyList(),
    val sort: SortOrder = SortOrder.LAST_ONLINE_DESC,
    val page: PageRequest = PageRequest(),
)

data class NotificationsQuery(
    val userId: UUID,
    val isRead: Boolean? = null,
    val category: NotificationCategory? = null,
    val page: PageRequest = PageRequest(),
)

