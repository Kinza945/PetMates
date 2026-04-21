package com.kynzai.domain.models

import java.time.Instant
import java.util.UUID

data class Invite(
    val inviteId: UUID,
    val userId: UUID,
    val projectId: UUID,
    val role: String,
    val status: InviteStatus = InviteStatus.PENDING,
    val createdAt: Instant? = null,
)

enum class InviteStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    CANCELLED,
}

