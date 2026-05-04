package com.kynzai.data.mock

import com.kynzai.data.repositories.mock.MockInviteRepository
import com.kynzai.data.repositories.mock.MockNotificationRepository
import com.kynzai.data.repositories.mock.MockProjectRepository
import com.kynzai.data.repositories.mock.MockResponseRepository
import com.kynzai.data.repositories.mock.MockUserRepository
import com.kynzai.domain.models.Gender
import com.kynzai.domain.models.InviteStatus
import com.kynzai.domain.models.UserProfileUpdate
import com.kynzai.domain.repositories.InviteRepository
import com.kynzai.domain.common.NotificationsQuery
import com.kynzai.domain.common.PageRequest
import com.kynzai.domain.common.PageToken
import com.kynzai.domain.common.ProjectFeedQuery
import com.kynzai.domain.repositories.NotificationRepository
import com.kynzai.domain.repositories.ProjectRepository
import com.kynzai.domain.repositories.ResponseRepository
import com.kynzai.domain.repositories.UserRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class MockRepositoriesTest {
    @Test
    fun user_search_blank_returns_all() {
        val data = FakeDataSource()
        val repo: UserRepository = MockUserRepository(data)

        runBlocking {
            val res = repo.searchUsers("").getOrThrow()
            assertEquals(data.users.size, res.size)
        }
    }

    @Test
    fun feed_pagination_works_with_default_impl() {
        val data = FakeDataSource()
        val repo: ProjectRepository = MockProjectRepository(data)

        runBlocking {
            val page1 = repo.getFeedProjects(ProjectFeedQuery(page = PageRequest(limit = 2))).getOrThrow()
            assertEquals(2, page1.items.size)
            assertTrue(page1.nextToken is PageToken.Offset)

            val page2 = repo.getFeedProjects(
                ProjectFeedQuery(page = PageRequest(limit = 2, token = page1.nextToken))
            ).getOrThrow()
            assertTrue(page2.items.isNotEmpty())
        }
    }

    @Test
    fun create_response_requires_authorization() {
        val data = FakeDataSource()
        data.currentUserId = null
        val repo: ResponseRepository = MockResponseRepository(data)

        val vacancyId = data.vacancies.first().vacancyId
        runBlocking {
            val res = repo.createResponse(vacancyId)
            assertTrue(res.isFailure)
        }
    }

    @Test
    fun notifications_filter_and_delete_work() {
        val data = FakeDataSource()
        val repo: NotificationRepository = MockNotificationRepository(data)

        val userId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        runBlocking {
            val page = repo.getNotifications(
                NotificationsQuery(
                    userId = userId,
                    isRead = false,
                    page = PageRequest(limit = 50),
                )
            ).getOrThrow()

            assertTrue(page.items.isNotEmpty())

            val firstId = page.items.first().notificationId
            assertTrue(repo.deleteNotification(firstId).isSuccess)
        }
    }

    @Test
    fun pending_response_cannot_be_created_twice_and_can_be_cancelled() {
        val data = FakeDataSource()
        data.currentUserId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")
        val repo: ResponseRepository = MockResponseRepository(data)

        val userId = data.currentUserId!!
        val vacancy = data.vacancies.first { vacancy ->
            val project = data.projects.first { it.projectId == vacancy.projectId }
            project.ownerId != userId &&
                data.projectMembers.none { it.projectId == project.projectId && it.userId == userId } &&
                data.responses.none { it.vacancyId == vacancy.vacancyId && it.userId == userId }
        }

        runBlocking {
            val created = repo.createResponse(vacancy.vacancyId).getOrThrow()
            assertTrue(repo.createResponse(vacancy.vacancyId).isFailure)

            val beforeNotifications = data.notifications.size
            assertTrue(repo.cancelResponse(created.responseId).isSuccess)
            assertFalse(data.responses.any { it.responseId == created.responseId })
            assertTrue(data.notifications.size > beforeNotifications)
        }
    }

    @Test
    fun invite_duplicate_is_rejected_and_cancel_marks_cancelled() {
        val data = FakeDataSource()
        val repo: InviteRepository = MockInviteRepository(data)
        val projectId = data.projects.first().projectId
        val userId = data.users.last().userId

        runBlocking {
            val invite = repo.createInvite(projectId, userId, "Designer").getOrThrow()
            assertTrue(repo.createInvite(projectId, userId, "Designer").isFailure)

            val cancelled = repo.cancelInvite(invite.inviteId).getOrThrow()
            assertEquals(InviteStatus.CANCELLED, cancelled.status)
        }
    }

    @Test
    fun unread_count_and_rate_project_rules_work() {
        val data = FakeDataSource()
        val notifications: NotificationRepository = MockNotificationRepository(data)
        val projects: ProjectRepository = MockProjectRepository(data)
        val project = data.projects.first()

        runBlocking {
            val unread = notifications.getUnreadCount(project.ownerId).getOrThrow()
            assertEquals(data.notifications.count { it.userId == project.ownerId && !it.isRead }, unread)

            data.currentUserId = project.ownerId
            assertTrue(projects.rateProject(project.projectId).isFailure)

            data.currentUserId = data.users.first { user ->
                user.userId != project.ownerId &&
                    data.projectRatings.none { it.projectId == project.projectId && it.userId == user.userId }
            }.userId
            assertTrue(projects.rateProject(project.projectId).isSuccess)
            assertTrue(projects.rateProject(project.projectId).isFailure)
            assertTrue(projects.getProjectRatings(project.projectId).getOrThrow().isNotEmpty())
        }
    }

    @Test
    fun update_profile_changes_current_user() {
        val data = FakeDataSource()
        val repo: UserRepository = MockUserRepository(data)

        runBlocking {
            val updated = repo.updateProfile(
                UserProfileUpdate(
                    realName = "Updated User",
                    age = 23,
                    gender = Gender.UNSPECIFIED,
                    country = "Россия",
                    city = "Краснодар",
                    workplace = "ИМСИТ",
                    profileRole = "Android Developer",
                    description = "Updated description",
                    hardSkills = listOf("#kotlin"),
                    softSkills = listOf("#teamwork"),
                )
            ).getOrThrow()

            assertEquals("Updated User", updated.realName)
            assertEquals(listOf("#kotlin"), updated.hardSkills)
        }
    }
}
