package com.kynzai.domain.repositories

import com.kynzai.domain.models.Invite
import com.kynzai.domain.models.InviteStatus
import java.util.UUID

interface InviteRepository {
    suspend fun getInvitesByProject(projectId: UUID): Result<List<Invite>>
    suspend fun getInvitesByUser(userId: UUID): Result<List<Invite>>

    /**
     * Отправка приглашения пользователю в проект.
     */
    suspend fun createInvite(
        projectId: UUID,
        userId: UUID,
        role: String,
        message: String? = null,
    ): Result<Invite> =
        Result.failure(UnsupportedOperationException("createInvite is not implemented"))

    suspend fun updateInviteStatus(inviteId: UUID, status: InviteStatus): Result<Invite>

    suspend fun cancelInvite(inviteId: UUID): Result<Invite> =
        Result.failure(UnsupportedOperationException("cancelInvite is not implemented"))
}
