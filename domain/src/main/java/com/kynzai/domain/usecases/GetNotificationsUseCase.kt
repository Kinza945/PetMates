package com.kynzai.domain.usecases

import com.kynzai.domain.common.AppResult
import com.kynzai.domain.common.NotificationsQuery
import com.kynzai.domain.common.Page
import com.kynzai.domain.common.toAppResult
import com.kynzai.domain.models.Notification
import com.kynzai.domain.repositories.NotificationRepository

class GetNotificationsUseCase(
    private val notifications: NotificationRepository,
) {
    suspend operator fun invoke(query: NotificationsQuery): AppResult<Page<Notification>> =
        notifications.getNotifications(query).toAppResult()
}

