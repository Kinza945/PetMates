package com.kynzai.domain.repositories

import com.kynzai.domain.models.Invite
import com.kynzai.domain.models.InviteStatus
import java.util.UUID

interface InviteRepository {
    suspend fun getInvitesByProject(projectId: UUID): Result<List<Invite>>
    suspend fun getInvitesByUser(userId: UUID): Result<List<Invite>>
    suspend fun updateInviteStatus(inviteId: UUID, status: InviteStatus): Result<Invite>
}

