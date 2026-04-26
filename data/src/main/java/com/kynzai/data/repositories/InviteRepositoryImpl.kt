package com.kynzai.data.repositories

import com.kynzai.data.remote.JSONArrayObjects
import com.kynzai.data.remote.SupabaseRestApi
import com.kynzai.data.remote.dto.InviteDto
import com.kynzai.data.remote.firstObjectFromArray
import com.kynzai.data.remote.mapper.toDomain
import com.kynzai.data.remote.toWire
import com.kynzai.domain.models.Invite
import com.kynzai.domain.models.InviteStatus
import com.kynzai.domain.repositories.InviteRepository
import org.json.JSONArray
import org.json.JSONObject
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

    override suspend fun createInvite(
        projectId: UUID,
        userId: UUID,
        role: String,
        message: String?,
    ): Result<Invite> =
        api.postTableJson(
            table = "invites",
            bodyJson = JSONObject()
                .put("project_id", projectId.toString())
                .put("user_id", userId.toString())
                .put("role", role)
                .put("status", InviteStatus.PENDING.toWire())
                .toString(),
            query = mapOf("select" to "*")
        ).mapCatching { raw ->
            InviteDto.fromJson(firstObjectFromArray(raw)).toDomain()
        }

    override suspend fun updateInviteStatus(inviteId: UUID, status: InviteStatus): Result<Invite> =
        api.patchTableJson(
            table = "invites",
            bodyJson = JSONObject()
                .put("status", status.toWire())
                .toString(),
            query = mapOf(
                "invite_id" to "eq.$inviteId",
                "select" to "*",
            )
        ).mapCatching { raw ->
            InviteDto.fromJson(firstObjectFromArray(raw)).toDomain()
        }

    override suspend fun cancelInvite(inviteId: UUID): Result<Invite> =
        updateInviteStatus(inviteId, InviteStatus.CANCELLED)
}
