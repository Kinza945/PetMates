package com.kynzai.data.repositories

import com.kynzai.data.remote.JSONArrayObjects
import com.kynzai.data.remote.SupabaseRestApi
import com.kynzai.data.remote.dto.InviteDto
import com.kynzai.data.remote.mapper.toDomain
import com.kynzai.data.remote.objectFromRpc
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
        api.postRpcJson(
            functionName = "invite_user",
            bodyJson = JSONObject()
                .put("p_project_id", projectId.toString())
                .put("p_user_id", userId.toString())
                .put("p_role", role)
                .put("p_message", message ?: JSONObject.NULL)
                .toString()
        ).mapCatching { raw ->
            InviteDto.fromJson(objectFromRpc(raw)).toDomain()
        }

    override suspend fun updateInviteStatus(inviteId: UUID, status: InviteStatus): Result<Invite> =
        api.postRpcJson(
            functionName = "update_invite_status",
            bodyJson = JSONObject()
                .put("p_invite_id", inviteId.toString())
                .put("p_status", status.toWire())
                .toString()
        ).mapCatching { raw ->
            InviteDto.fromJson(objectFromRpc(raw)).toDomain()
        }

    override suspend fun cancelInvite(inviteId: UUID): Result<Invite> =
        api.postRpcJson(
            functionName = "cancel_invite",
            bodyJson = JSONObject()
                .put("p_invite_id", inviteId.toString())
                .toString()
        ).mapCatching { raw ->
            InviteDto.fromJson(objectFromRpc(raw)).toDomain()
        }
}
