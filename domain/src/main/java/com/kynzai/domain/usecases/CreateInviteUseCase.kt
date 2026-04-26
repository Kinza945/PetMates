package com.kynzai.domain.usecases

import com.kynzai.domain.common.AppResult
import com.kynzai.domain.common.toAppResult
import com.kynzai.domain.models.Invite
import com.kynzai.domain.repositories.InviteRepository
import java.util.UUID

class CreateInviteUseCase(
    private val invites: InviteRepository,
) {
    suspend operator fun invoke(
        projectId: UUID,
        userId: UUID,
        role: String,
        message: String?,
    ): AppResult<Invite> =
        invites.createInvite(
            projectId = projectId,
            userId = userId,
            role = role,
            message = message,
        ).toAppResult()
}

