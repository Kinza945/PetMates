package com.kynzai.data.remote

import com.kynzai.data.network.BackendConfig
import com.kynzai.data.network.SupabaseConfig
import com.kynzai.data.repositories.InviteRepositoryImpl
import com.kynzai.data.repositories.ProjectRepositoryImpl
import com.kynzai.data.repositories.ResponseRepositoryImpl
import com.kynzai.data.repositories.UserRepositoryImpl
import com.kynzai.domain.models.Gender
import com.kynzai.domain.models.InviteStatus
import com.kynzai.domain.models.ResponseStatus
import com.kynzai.domain.models.UserProfileUpdate
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseRpcRepositoryTest {
    @Test
    fun createResponse_uses_respondToVacancyRpc() = runBlocking {
        val calls = mutableListOf<String>()
        val repo = ResponseRepositoryImpl(api(calls, responseJson()))

        repo.createResponse(VACANCY_ID).getOrThrow()

        assertTrue(calls.single().contains("/rest/v1/rpc/respond_to_vacancy"))
    }

    @Test
    fun updateResponseStatus_uses_updateResponseStatusRpc() = runBlocking {
        val calls = mutableListOf<String>()
        val repo = ResponseRepositoryImpl(api(calls, responseJson(status = "accepted")))

        repo.updateResponseStatus(RESPONSE_ID, ResponseStatus.ACCEPTED).getOrThrow()

        assertTrue(calls.single().contains("/rest/v1/rpc/update_response_status"))
    }

    @Test
    fun createInvite_uses_inviteUserRpc() = runBlocking {
        val calls = mutableListOf<String>()
        val repo = InviteRepositoryImpl(api(calls, inviteJson()))

        repo.createInvite(PROJECT_ID, USER_ID, "Backend", "Присоединяйся").getOrThrow()

        assertTrue(calls.single().contains("/rest/v1/rpc/invite_user"))
    }

    @Test
    fun cancelInvite_uses_cancelInviteRpc() = runBlocking {
        val calls = mutableListOf<String>()
        val repo = InviteRepositoryImpl(api(calls, inviteJson(status = "cancelled")))

        repo.cancelInvite(INVITE_ID).getOrThrow()

        assertTrue(calls.single().contains("/rest/v1/rpc/cancel_invite"))
    }

    @Test
    fun updateInviteStatus_uses_updateInviteStatusRpc() = runBlocking {
        val calls = mutableListOf<String>()
        val repo = InviteRepositoryImpl(api(calls, inviteJson(status = "accepted")))

        repo.updateInviteStatus(INVITE_ID, InviteStatus.ACCEPTED).getOrThrow()

        assertTrue(calls.single().contains("/rest/v1/rpc/update_invite_status"))
    }

    @Test
    fun rateProject_uses_rateProjectRpc() = runBlocking {
        val calls = mutableListOf<String>()
        val repo = ProjectRepositoryImpl(api(calls, projectJson()))

        repo.rateProject(PROJECT_ID).getOrThrow()

        assertTrue(calls.single().contains("/rest/v1/rpc/rate_project"))
    }

    @Test
    fun updateProfile_uses_updateMyProfileRpc() = runBlocking {
        val calls = mutableListOf<String>()
        val repo = UserRepositoryImpl(api(calls, userJson()), disabledBackendApi())

        repo.updateProfile(
            UserProfileUpdate(
                realName = "Test User",
                age = 21,
                gender = Gender.UNSPECIFIED,
                country = "Россия",
            )
        ).getOrThrow()

        assertTrue(calls.single().contains("/rest/v1/rpc/update_my_profile"))
    }

    private fun api(calls: MutableList<String>, responseBody: String): SupabaseRestApi =
        SupabaseRestApi(
            http = HttpClient(
                MockEngine { request ->
                    calls += request.url.toString()
                    respond(
                        content = responseBody,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            ),
            config = SupabaseConfig(
                baseUrl = "https://petmates-test.supabase.co",
                anonKey = "anon-key",
            ),
        )

    private fun disabledBackendApi(): BackendApi {
        val client = HttpClient(
            MockEngine {
                error("Backend API must stay disabled in this Supabase RPC test")
            }
        )
        return BackendApi(
            http = client,
            authHttp = client,
            config = BackendConfig(baseUrl = ""),
        )
    }

    private fun responseJson(status: String = "pending"): String =
        """
        {
          "response_id": "$RESPONSE_ID",
          "user_id": "$USER_ID",
          "vacancy_id": "$VACANCY_ID",
          "status": "$status",
          "created_at": "2026-04-01T10:00:00Z"
        }
        """.trimIndent()

    private fun inviteJson(status: String = "pending"): String =
        """
        {
          "invite_id": "$INVITE_ID",
          "user_id": "$USER_ID",
          "project_id": "$PROJECT_ID",
          "role": "Backend",
          "status": "$status",
          "created_at": "2026-04-01T10:00:00Z"
        }
        """.trimIndent()

    private fun projectJson(): String =
        """
        {
          "project_id": "$PROJECT_ID",
          "owner_id": "$OWNER_ID",
          "name": "PetMates",
          "short_description": "Команда для pet-проектов",
          "full_description": "Описание",
          "status": "in_progress",
          "status_changed_at": "2026-04-01T10:00:00Z",
          "rating_count": 5,
          "created_at": "2026-04-01T10:00:00Z"
        }
        """.trimIndent()

    private fun userJson(): String =
        """
        {
          "user_id": "$USER_ID",
          "nickname": "test_user",
          "avatar_url": null,
          "real_name": "Test User",
          "age": 21,
          "gender": "unspecified",
          "country": "Россия",
          "city": "Краснодар",
          "workplace": "ИМСИТ",
          "profile_role": "Android",
          "system_role": "user",
          "description": "Описание",
          "hard_skills": ["#kotlin"],
          "soft_skills": ["#teamwork"],
          "contacts": [{"name": "Telegram", "link": "@test"}],
          "last_online_at": "2026-04-01T10:00:00Z",
          "created_at": "2026-04-01T10:00:00Z"
        }
        """.trimIndent()

    private companion object {
        val USER_ID: UUID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        val OWNER_ID: UUID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
        val PROJECT_ID: UUID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")
        val VACANCY_ID: UUID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")
        val RESPONSE_ID: UUID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")
        val INVITE_ID: UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")
    }
}
