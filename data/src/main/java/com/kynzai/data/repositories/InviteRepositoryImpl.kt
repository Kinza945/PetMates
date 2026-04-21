package com.kynzai.data.repositories

import com.kynzai.data.remote.JSONArrayObjects
import com.kynzai.data.remote.SupabaseRestApi
import com.kynzai.data.remote.dto.InviteDto
import com.kynzai.data.remote.mapper.toDomain
import com.kynzai.domain.models.Invite
import com.kynzai.domain.models.InviteStatus
import com.kynzai.domain.repositories.InviteRepository
import org.json.JSONArray
import java.util.UUID
import javax.inject.Inject

class InviteRepositoryImpl @Inject constructor(
    private val api: SupabaseRestApi,
) : InviteRepository {
    override suspend fun getInvitesByProject(projectId: UUID): Result<List<Invite>> =
        api.getTableJson(
            table = "invites",
            query = mapOf(
                "select" to "*",
                "project_id" to "eq.$projectId"
            )
        ).mapCatching { raw ->
            val arr = JSONArray(raw)
            JSONArrayObjects(arr)
                .map { InviteDto.fromJson(it).toDomain() }
                .toList()
        }

    override suspend fun getInvitesByUser(userId: UUID): Result<List<Invite>> =
        api.getTableJson(
            table = "invites",
            query = mapOf(
                "select" to "*",
                "user_id" to "eq.$userId"
            )
        ).mapCatching { raw ->
            val arr = JSONArray(raw)
            JSONArrayObjects(arr)
                .map { InviteDto.fromJson(it).toDomain() }
                .toList()
        }

    override suspend fun updateInviteStatus(inviteId: UUID, status: InviteStatus): Result<Invite> =
        Result.failure(NotImplementedError("PATCH /invites not implemented yet"))
}

