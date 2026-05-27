package com.kynzai.data.repositories.mock

import com.kynzai.data.mock.FakeDataSource
import com.kynzai.domain.common.CachedResource
import com.kynzai.domain.models.User
import com.kynzai.domain.models.UserProfileUpdate
import com.kynzai.domain.repositories.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import javax.inject.Inject

class MockUserRepository @Inject constructor(
    private val data: FakeDataSource,
) : UserRepository {
    override fun observeMyProfile(forceRefresh: Boolean): Flow<CachedResource<User>> {
        val user = data.currentUserId?.let { id -> data.users.firstOrNull { it.userId == id } }
        return flowOf(
            if (user != null) {
                CachedResource(data = user)
            } else {
                CachedResource(error = IllegalStateException("Unauthorized"))
            }
        )
    }

    override suspend fun getUserById(userId: UUID): Result<User> =
        data.users.firstOrNull { it.userId == userId }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("User not found: $userId"))

    override suspend fun getUserByNickname(nickname: String): Result<User> =
        data.users.firstOrNull { it.nickname.equals(nickname, ignoreCase = true) }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("User not found: $nickname"))

    override suspend fun searchUsers(query: String): Result<List<User>> {
        val q = query.trim()
        if (q.isBlank()) return Result.success(data.users.toList())
        val filtered = data.users.filter { u ->
            u.nickname.contains(q, ignoreCase = true) ||
                (u.profileRole?.contains(q, ignoreCase = true) == true) ||
                u.hardSkills.any { it.contains(q, ignoreCase = true) } ||
                u.softSkills.any { it.contains(q, ignoreCase = true) }
        }
        return Result.success(filtered)
    }

    override suspend fun updateProfile(update: UserProfileUpdate): Result<User> {
        val userId = data.currentUserId ?: return Result.failure(IllegalStateException("Unauthorized"))
        val idx = data.users.indexOfFirst { it.userId == userId }
        if (idx == -1) return Result.failure(NoSuchElementException("User not found: $userId"))

        val updated = data.users[idx].copy(
            realName = update.realName,
            age = update.age,
            gender = update.gender,
            country = update.country,
            city = update.city,
            workplace = update.workplace,
            profileRole = update.profileRole,
            description = update.description,
            hardSkills = update.hardSkills,
            softSkills = update.softSkills,
            contacts = update.contacts,
        )
        data.users[idx] = updated
        return Result.success(updated)
    }

    override suspend fun deleteAccount(): Result<Unit> {
        val userId = data.currentUserId ?: return Result.failure(IllegalStateException("Unauthorized"))
        val ownedProjectIds = data.projects
            .filter { it.ownerId == userId }
            .map { it.projectId }
            .toSet()
        val ownedVacancyIds = data.vacancies
            .filter { it.projectId in ownedProjectIds }
            .map { it.vacancyId }
            .toSet()

        data.responses.removeAll { it.userId == userId || it.vacancyId in ownedVacancyIds }
        data.invites.removeAll { it.userId == userId || it.projectId in ownedProjectIds }
        data.notifications.removeAll { it.userId == userId || it.referenceId in ownedProjectIds }
        data.projectMembers.removeAll { it.userId == userId || it.projectId in ownedProjectIds }
        data.vacancies.removeAll { it.projectId in ownedProjectIds }
        data.projectRatings.removeAll { it.userId == userId || it.projectId in ownedProjectIds }
        data.projects.removeAll { it.ownerId == userId }
        data.users.removeAll { it.userId == userId }
        data.currentUserId = null

        return Result.success(Unit)
    }
}
