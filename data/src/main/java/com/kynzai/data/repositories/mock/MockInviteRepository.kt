package com.kynzai.data.repositories.mock

import com.kynzai.data.mock.FakeDataSource
import com.kynzai.domain.models.Invite
import com.kynzai.domain.models.InviteStatus
import com.kynzai.domain.models.Notification
import com.kynzai.domain.models.NotificationCategory
import com.kynzai.domain.models.ReferenceType
import com.kynzai.domain.repositories.InviteRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class MockInviteRepository @Inject constructor(
    private val data: FakeDataSource,
) : InviteRepository {
    override suspend fun getInvitesByProject(projectId: UUID): Result<List<Invite>> =
        Result.success(data.invites.filter { it.projectId == projectId })

    override suspend fun getInvitesByUser(userId: UUID): Result<List<Invite>> =
        Result.success(data.invites.filter { it.userId == userId })

    override suspend fun createInvite(
        projectId: UUID,
        userId: UUID,
        role: String,
        message: String?,
    ): Result<Invite> {
        val already = data.invites.any { it.projectId == projectId && it.userId == userId && it.status == InviteStatus.PENDING }
        if (already) return Result.failure(IllegalStateException("Invite already exists"))

        val now = Instant.now()
        val invite = Invite(
            inviteId = UUID.randomUUID(),
            userId = userId,
            projectId = projectId,
            role = role.trim(),
            status = InviteStatus.PENDING,
            createdAt = now,
        )
        data.invites.add(0, invite)

        val project = data.projects.firstOrNull { it.projectId == projectId }
        val projectName = project?.name

        // Уведомляем приглашённого пользователя.
        data.notifications.add(
            0,
            Notification(
                notificationId = UUID.randomUUID(),
                userId = userId,
                category = NotificationCategory.INVITATION,
                eventType = "invite.created",
                referenceType = ReferenceType.INVITATION,
                referenceId = invite.inviteId,
                contextData = buildMap {
                    if (projectName != null) put("project_name", projectName)
                    put("role", invite.role)
                    if (!message.isNullOrBlank()) put("message", message)
                },
                isRead = false,
                createdAt = now,
            )
        )

        // Уведомляем владельца проекта о созданном приглашении.
        val ownerId = project?.ownerId
        if (ownerId != null) {
            data.notifications.add(
                0,
                Notification(
                    notificationId = UUID.randomUUID(),
                    userId = ownerId,
                    category = NotificationCategory.INVITATION,
                    eventType = "invite.created",
                    referenceType = ReferenceType.INVITATION,
                    referenceId = invite.inviteId,
                    contextData = buildMap {
                        if (projectName != null) put("project_name", projectName)
                        put("user_id", userId.toString())
                        put("role", invite.role)
                    },
                    isRead = false,
                    createdAt = now,
                )
            )
        }

        return Result.success(invite)
    }

    override suspend fun updateInviteStatus(inviteId: UUID, status: InviteStatus): Result<Invite> {
        val idx = data.invites.indexOfFirst { it.inviteId == inviteId }
        if (idx == -1) return Result.failure(NoSuchElementException("Invite not found: $inviteId"))

        val updated = data.invites[idx].copy(status = status)
        data.invites[idx] = updated

        // При смене статуса уведомляем обе стороны. В mock-слое это best-effort логика.
        val project = data.projects.firstOrNull { it.projectId == updated.projectId }
        val ownerId = project?.ownerId
        if (ownerId != null) {
            data.notifications.add(
                0,
                Notification(
                    notificationId = UUID.randomUUID(),
                    userId = ownerId,
                    category = NotificationCategory.INVITATION,
                    eventType = "invite.${status.name.lowercase()}",
                    referenceType = ReferenceType.INVITATION,
                    referenceId = updated.inviteId,
                    contextData = mapOf(
                        "project_name" to project.name,
                        "user_id" to updated.userId.toString(),
                    ),
                    isRead = false,
                    createdAt = Instant.now(),
                )
            )
        }

        data.notifications.add(
            0,
            Notification(
                notificationId = UUID.randomUUID(),
                userId = updated.userId,
                category = NotificationCategory.INVITATION,
                eventType = "invite.${status.name.lowercase()}",
                referenceType = ReferenceType.INVITATION,
                referenceId = updated.inviteId,
                contextData = buildMap {
                    if (project != null) put("project_name", project.name)
                },
                isRead = false,
                createdAt = Instant.now(),
            )
        )

        return Result.success(updated)
    }

    override suspend fun cancelInvite(inviteId: UUID): Result<Invite> =
        updateInviteStatus(inviteId, InviteStatus.CANCELLED)
}
