package com.kynzai.data.mock

import com.kynzai.domain.models.Contact
import com.kynzai.domain.models.Gender
import com.kynzai.domain.models.Invite
import com.kynzai.domain.models.InviteStatus
import com.kynzai.domain.models.Notification
import com.kynzai.domain.models.NotificationCategory
import com.kynzai.domain.models.Project
import com.kynzai.domain.models.ProjectMember
import com.kynzai.domain.models.ProjectStatus
import com.kynzai.domain.models.ReferenceType
import com.kynzai.domain.models.Response
import com.kynzai.domain.models.ResponseStatus
import com.kynzai.domain.models.SystemRole
import com.kynzai.domain.models.User
import com.kynzai.domain.models.Vacancy
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue

@Singleton
class FakeDataSource @Inject constructor() {
    /*
     * Локальный in-memory backend на этапе разработки без сервера.
     *
     * Mock-репозитории изменяют эти коллекции так же, как реальный сервер
     * изменял бы таблицы: создаются/отменяются отклики, приглашения меняют статус,
     * уведомления читаются/удаляются, профиль обновляется. Благодаря этому UI и
     * ViewModel можно тестировать сейчас, а позже переключить на real-репозитории
     * без переписывания экранов.
     */
    // В реальном приложении текущий пользователь придёт из Auth/JWT. В mock-режиме держим его здесь.
    var currentUserId: UUID? = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    // Пара (userId, projectId) нужна для правила: один пользователь оценивает проект только один раз.
    val projectRatings: MutableSet<Pair<UUID, UUID>> = mutableSetOf()

    private val baseNow: Instant = Instant.parse("2026-04-21T12:00:00Z")

    private fun stableUuid(key: String): UUID =
        UUID.nameUUIDFromBytes(key.toByteArray(Charsets.UTF_8))

    val users: MutableList<User> = mutableListOf(
        User(
            userId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            nickname = "kynzai",
            avatarUrl = null,
            realName = "Кирилл",
            age = 20,
            gender = Gender.MALE,
            country = "Россия",
            city = "Краснодар",
            workplace = "ИМСИТ",
            profileRole = "Android Developer",
            systemRole = SystemRole.USER,
            description = "Делаю PetMates. Kotlin, Compose, Clean Architecture.",
            hardSkills = listOf("#kotlin", "#compose", "#ktor", "#hilt", "#clean"),
            softSkills = listOf("#teamwork", "#ownership"),
            contacts = listOf(Contact("Telegram", "https://t.me/kynzai")),
            lastOnlineAt = Instant.parse("2026-04-21T07:00:00Z"),
            createdAt = Instant.parse("2026-02-01T10:00:00Z"),
        ),
        User(
            userId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            nickname = "ClownZzz",
            realName = "Вадим",
            age = 21,
            gender = Gender.MALE,
            country = "Россия",
            city = "Москва",
            workplace = "КУБ ГАУ",
            profileRole = "Python Data Science",
            systemRole = SystemRole.USER,
            description = "Ищу команду, делаю интересные штуки.",
            hardSkills = listOf("#python", "#pandas", "#sql"),
            softSkills = listOf("#communication"),
            contacts = listOf(Contact("GitHub", "https://github.com/dog_i1x")),
            lastOnlineAt = Instant.parse("2026-04-20T20:15:00Z"),
            createdAt = Instant.parse("2026-03-01T11:00:00Z"),
        ),
        User(
            userId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            nickname = "DesignMaster",
            realName = "Анна",
            age = 19,
            gender = Gender.FEMALE,
            country = "Россия",
            city = "Санкт-Петербург",
            workplace = "СПбГУ",
            profileRole = "UI/UX Designer",
            systemRole = SystemRole.USER,
            description = "Design systems, mobile UI, prototyping.",
            hardSkills = listOf("#figma", "#uiux", "#design"),
            softSkills = listOf("#empathy"),
            contacts = listOf(Contact("Telegram", "https://t.me/designmaster")),
            lastOnlineAt = Instant.parse("2026-04-21T06:40:00Z"),
            createdAt = Instant.parse("2026-03-12T08:00:00Z"),
        ),
        User(
            userId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
            nickname = "BackendEnjoyer",
            realName = "Илья",
            age = 22,
            gender = Gender.MALE,
            country = "Россия",
            city = "Казань",
            workplace = "КФУ",
            profileRole = "Backend Developer",
            systemRole = SystemRole.USER,
            description = "Ktor, Postgres, Supabase, high-load dreams.",
            hardSkills = listOf("#ktor", "#postgres", "#supabase"),
            softSkills = listOf("#responsibility"),
            contacts = listOf(Contact("GitHub", "https://github.com/backendenjoyer")),
            lastOnlineAt = Instant.parse("2026-04-21T07:20:00Z"),
            createdAt = Instant.parse("2026-03-20T08:00:00Z"),
        ),
    )

    val projects: MutableList<Project> = mutableListOf(
        Project(
            projectId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            ownerId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            name = "Приложение Contacts",
            shortDescription = "Контакты с умным поиском и синхронизацией.",
            fullDescription = "Подробное описание проекта Contacts.",
            status = ProjectStatus.IN_PROGRESS,
            statusChangedAt = Instant.parse("2026-04-10T10:00:00Z"),
            ratingCount = 4,
            createdAt = Instant.parse("2026-04-01T10:00:00Z"),
        ),
        Project(
            projectId = UUID.fromString("22222222-2222-2222-2222-222222222222"),
            ownerId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            name = "PetMates Mobile",
            shortDescription = "Мобильный клиент платформы PetMates.",
            fullDescription = "Clean Architecture, Compose UI, Ktor, Supabase.",
            status = ProjectStatus.IN_PROGRESS,
            statusChangedAt = Instant.parse("2026-04-18T10:00:00Z"),
            ratingCount = 12,
            createdAt = Instant.parse("2026-04-05T10:00:00Z"),
        ),
        Project(
            projectId = UUID.fromString("33333333-3333-3333-3333-333333333333"),
            ownerId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
            name = "Task Tracker API",
            shortDescription = "REST API для трекера задач на Ktor.",
            fullDescription = "Слои, миграции, JWT, тесты.",
            status = ProjectStatus.PAUSED,
            statusChangedAt = Instant.parse("2026-04-12T10:00:00Z"),
            ratingCount = 7,
            createdAt = Instant.parse("2026-04-02T10:00:00Z"),
        ),
    )

    val projectMembers: MutableList<ProjectMember> = mutableListOf(
        ProjectMember(
            memberId = UUID.fromString("aaaa1111-1111-1111-1111-111111111111"),
            projectId = UUID.fromString("22222222-2222-2222-2222-222222222222"),
            userId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            role = "Owner",
            joinedAt = Instant.parse("2026-04-05T10:00:00Z"),
        ),
        ProjectMember(
            memberId = UUID.fromString("bbbb1111-1111-1111-1111-111111111111"),
            projectId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            userId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            role = "Owner",
            joinedAt = Instant.parse("2026-04-01T10:00:00Z"),
        ),
    )

    val vacancies: MutableList<Vacancy> = mutableListOf(
        Vacancy(
            vacancyId = UUID.fromString("aaaa2222-2222-2222-2222-222222222222"),
            projectId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            title = "Frontend-разработчик",
            role = "Frontend",
            description = "Нужен React/верстка, аккуратные компоненты.",
            requiredTags = listOf("#web", "#react", "#hooks"),
            isOpen = true,
            publishedAt = Instant.parse("2026-04-10T12:00:00Z"),
        ),
        Vacancy(
            vacancyId = UUID.fromString("bbbb2222-2222-2222-2222-222222222222"),
            projectId = UUID.fromString("22222222-2222-2222-2222-222222222222"),
            title = "QA Engineer",
            role = "QA",
            description = "Тест-план + Compose UI tests.",
            requiredTags = listOf("#qa", "#testing", "#compose"),
            isOpen = true,
            publishedAt = Instant.parse("2026-04-15T12:00:00Z"),
        ),
        Vacancy(
            vacancyId = UUID.fromString("cccc2222-2222-2222-2222-222222222222"),
            projectId = UUID.fromString("33333333-3333-3333-3333-333333333333"),
            title = "Backend-разработчик",
            role = "Backend",
            description = "Помочь с миграциями и auth.",
            requiredTags = listOf("#ktor", "#postgres", "#jwt"),
            isOpen = false,
            publishedAt = Instant.parse("2026-04-08T12:00:00Z"),
        ),
    )

    val responses: MutableList<Response> = mutableListOf(
        Response(
            responseId = UUID.fromString("aaaa3333-3333-3333-3333-333333333333"),
            userId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            vacancyId = UUID.fromString("aaaa2222-2222-2222-2222-222222222222"),
            status = ResponseStatus.PENDING,
            createdAt = Instant.parse("2026-04-11T10:00:00Z"),
        ),
    )

    val invites: MutableList<Invite> = mutableListOf(
        Invite(
            inviteId = UUID.fromString("aaaa4444-4444-4444-4444-444444444444"),
            userId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            projectId = UUID.fromString("22222222-2222-2222-2222-222222222222"),
            role = "Designer",
            status = InviteStatus.PENDING,
            createdAt = Instant.parse("2026-04-16T10:00:00Z"),
        ),
    )

    val notifications: MutableList<Notification> = mutableListOf(
        Notification(
            notificationId = UUID.fromString("aaaa5555-5555-5555-5555-555555555555"),
            userId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            category = NotificationCategory.PROJECT,
            eventType = "project.status_changed",
            referenceType = ReferenceType.PROJECT,
            referenceId = UUID.fromString("22222222-2222-2222-2222-222222222222"),
            contextData = mapOf("project_name" to "PetMates Mobile", "status" to "in_progress"),
            isRead = false,
            createdAt = Instant.parse("2026-04-20T18:54:00Z"),
        ),
        Notification(
            notificationId = UUID.fromString("bbbb5555-5555-5555-5555-555555555555"),
            userId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            category = NotificationCategory.RESPONSE,
            eventType = "response.created",
            referenceType = ReferenceType.RESPONSE,
            referenceId = UUID.fromString("aaaa3333-3333-3333-3333-333333333333"),
            contextData = mapOf("project_name" to "Приложение Contacts", "vacancy_title" to "Frontend-разработчик"),
            isRead = false,
            createdAt = Instant.parse("2026-04-11T10:00:10Z"),
        ),
    )

    init {
        seedExtraData()
    }

    private fun seedExtraData() {
        // Дополнительные данные детерминированы: тесты и preview не будут "прыгать" между запусками.
        seedUsers()
        seedProjectsAndVacancies()
        seedResponsesInvitesNotifications()
    }

    private fun seedUsers() {
        data class Seed(
            val nickname: String,
            val realName: String,
            val role: String,
            val city: String,
            val country: String = "Россия",
            val workplace: String,
            val hard: List<String>,
            val soft: List<String>,
        )

        val seeds = listOf(
            Seed(
                nickname = "qa_cat",
                realName = "Екатерина",
                role = "QA Engineer",
                city = "Новосибирск",
                workplace = "НГУ",
                hard = listOf("#qa", "#testing", "#compose", "#mock"),
                soft = listOf("#detail", "#communication"),
            ),
            Seed(
                nickname = "ops_guy",
                realName = "Олег",
                role = "DevOps",
                city = "Екатеринбург",
                workplace = "УрФУ",
                hard = listOf("#docker", "#ci", "#k8s"),
                soft = listOf("#ownership", "#responsibility"),
            ),
            Seed(
                nickname = "swiftie",
                realName = "Мария",
                role = "iOS Developer",
                city = "Москва",
                workplace = "МФТИ",
                hard = listOf("#swift", "#swiftui", "#networking"),
                soft = listOf("#teamwork"),
            ),
            Seed(
                nickname = "data_fox",
                realName = "Никита",
                role = "Data Analyst",
                city = "Казань",
                workplace = "КФУ",
                hard = listOf("#sql", "#bi", "#analytics"),
                soft = listOf("#curiosity"),
            ),
            Seed(
                nickname = "web_tiger",
                realName = "Сергей",
                role = "Frontend Developer",
                city = "Санкт-Петербург",
                workplace = "ИТМО",
                hard = listOf("#react", "#typescript", "#css"),
                soft = listOf("#communication"),
            ),
            Seed(
                nickname = "ml_raccoon",
                realName = "Алексей",
                role = "ML Engineer",
                city = "Москва",
                workplace = "ВШЭ",
                hard = listOf("#python", "#pytorch", "#nlp"),
                soft = listOf("#research"),
            ),
            Seed(
                nickname = "ui_bird",
                realName = "Полина",
                role = "Product Designer",
                city = "Санкт-Петербург",
                workplace = "СПбГУ",
                hard = listOf("#figma", "#ux", "#prototyping"),
                soft = listOf("#empathy"),
            ),
            Seed(
                nickname = "kotlin_fan",
                realName = "Артём",
                role = "Android Developer",
                city = "Краснодар",
                workplace = "КубГУ",
                hard = listOf("#kotlin", "#coroutines", "#flow"),
                soft = listOf("#ownership"),
            ),
            Seed(
                nickname = "api_wizard",
                realName = "Денис",
                role = "Backend Developer",
                city = "Казань",
                workplace = "КФУ",
                hard = listOf("#rest", "#auth", "#caching"),
                soft = listOf("#responsibility"),
            ),
            Seed(
                nickname = "db_otter",
                realName = "Егор",
                role = "DB Engineer",
                city = "Екатеринбург",
                workplace = "УрФУ",
                hard = listOf("#postgres", "#indexes", "#migrations"),
                soft = listOf("#detail"),
            ),
            Seed(
                nickname = "pm_ninja",
                realName = "Ирина",
                role = "Product Manager",
                city = "Москва",
                workplace = "ВШЭ",
                hard = listOf("#roadmap", "#backlog", "#metrics"),
                soft = listOf("#communication"),
            ),
            Seed(
                nickname = "support_bear",
                realName = "Глеб",
                role = "Community Manager",
                city = "Краснодар",
                workplace = "ИМСИТ",
                hard = listOf("#support", "#moderation", "#content"),
                soft = listOf("#empathy"),
            ),
        )

        seeds.forEachIndexed { idx, s ->
            if (users.any { it.nickname.equals(s.nickname, ignoreCase = true) }) return@forEachIndexed
            val id = stableUuid("user:${s.nickname}")
            val online = baseNow.minusSeconds(((idx + 1) * 17L * 60L) % (48L * 60L * 60L))
            users.add(
                User(
                    userId = id,
                    nickname = s.nickname,
                    realName = s.realName,
                    age = 18 + (idx % 8),
                    gender = when (idx % 3) {
                        0 -> Gender.MALE
                        1 -> Gender.FEMALE
                        else -> Gender.UNSPECIFIED
                    },
                    country = s.country,
                    city = s.city,
                    workplace = s.workplace,
                    profileRole = s.role,
                    systemRole = SystemRole.USER,
                    description = "Профиль ${s.nickname}. (мок-данные)",
                    hardSkills = s.hard,
                    softSkills = s.soft,
                    contacts = listOf(
                        Contact("Telegram", "https://t.me/${s.nickname}"),
                        Contact("GitHub", "https://github.com/${s.nickname}")
                    ),
                    lastOnlineAt = online,
                    createdAt = baseNow.minusSeconds((60L * 60L * 24L) * (30L + idx.toLong())),
                )
            )
        }
    }

    private fun seedProjectsAndVacancies() {
        data class ProjectSeed(
            val name: String,
            val ownerNickname: String,
            val short: String,
            val full: String,
            val status: ProjectStatus,
            val rating: Int,
            val createdDaysAgo: Long,
        )

        val seeds = listOf(
            ProjectSeed(
                name = "Mobile Design System",
                ownerNickname = "DesignMaster",
                short = "Библиотека компонентов и токены для мобильного UI.",
                full = "Делаем набор компонентов, темы, типографику и правила для Compose.",
                status = ProjectStatus.IN_PROGRESS,
                rating = 2,
                createdDaysAgo = 15,
            ),
            ProjectSeed(
                name = "CI для PetMates",
                ownerNickname = "ops_guy",
                short = "Пайплайны сборки, тестов и релизов.",
                full = "Настраиваем Gradle кеши, UI-тесты, отчёты и артефакты.",
                status = ProjectStatus.IN_PROGRESS,
                rating = 1,
                createdDaysAgo = 9,
            ),
            ProjectSeed(
                name = "Чат бот семейного ресторана",
                ownerNickname = "api_wizard",
                short = "Бот для заказов/броней + админка.",
                full = "Нужны сценарии, интеграции и понятная структура.",
                status = ProjectStatus.PAUSED,
                rating = 4,
                createdDaysAgo = 35,
            ),
            ProjectSeed(
                name = "PetMates Web",
                ownerNickname = "web_tiger",
                short = "Веб-клиент для платформы PetMates.",
                full = "React, дизайн-система и интеграция с API.",
                status = ProjectStatus.IN_PROGRESS,
                rating = 8,
                createdDaysAgo = 22,
            ),
            ProjectSeed(
                name = "Supabase Schema Review",
                ownerNickname = "db_otter",
                short = "Проектирование схемы и RLS-политик.",
                full = "Оптимизация таблиц, индексов и триггеров.",
                status = ProjectStatus.COMPLETED,
                rating = 6,
                createdDaysAgo = 60,
            ),
            ProjectSeed(
                name = "ML: Pet Image Tags",
                ownerNickname = "ml_raccoon",
                short = "Классификация и теги для фото питомцев.",
                full = "Сбор датасета, baseline модели, инференс.",
                status = ProjectStatus.IN_PROGRESS,
                rating = 5,
                createdDaysAgo = 12,
            ),
        )

        fun userIdByNickname(nick: String): UUID? =
            users.firstOrNull { it.nickname.equals(nick, ignoreCase = true) }?.userId

        seeds.forEachIndexed { idx, s ->
            val projectId = stableUuid("project:${s.name}")
            if (projects.any { it.projectId == projectId }) return@forEachIndexed

            val ownerId = userIdByNickname(s.ownerNickname) ?: return@forEachIndexed
            val createdAt = baseNow.minusSeconds((60L * 60L * 24L) * s.createdDaysAgo)
            val project = Project(
                projectId = projectId,
                ownerId = ownerId,
                name = s.name,
                shortDescription = s.short,
                fullDescription = s.full,
                status = s.status,
                statusChangedAt = createdAt.plusSeconds(60L * 60L * 12L),
                ratingCount = s.rating,
                createdAt = createdAt,
            )
            projects.add(project)

            projectMembers.add(
                ProjectMember(
                    memberId = stableUuid("member:${projectId}:${ownerId}"),
                    projectId = projectId,
                    userId = ownerId,
                    role = "Owner",
                    joinedAt = createdAt,
                )
            )

            // 1-2 вакансии на проект делают ленту похожей на реальное приложение.
            val vacancySeeds = listOf(
                "Android разработчик" to listOf("#kotlin", "#compose", "#coroutines"),
                "Frontend-разработчик" to listOf("#web", "#react", "#typescript"),
                "Backend-разработчик" to listOf("#ktor", "#postgres", "#jwt"),
                "QA Engineer" to listOf("#qa", "#testing", "#compose"),
                "UI/UX дизайнер" to listOf("#figma", "#uiux", "#design"),
            )

            val v1 = vacancySeeds[(projectId.hashCode().absoluteValue + idx) % vacancySeeds.size]
            val v2 = vacancySeeds[(projectId.hashCode().absoluteValue + idx + 1) % vacancySeeds.size]

            vacancies.add(
                Vacancy(
                    vacancyId = stableUuid("vacancy:${projectId}:1"),
                    projectId = projectId,
                    title = v1.first,
                    role = v1.first.substringBefore(' ').take(20),
                    description = "Вакансия для проекта «${s.name}». (мок-данные)",
                    requiredTags = v1.second,
                    isOpen = true,
                    publishedAt = createdAt.plusSeconds(60L * 60L * 24L),
                )
            )
            vacancies.add(
                Vacancy(
                    vacancyId = stableUuid("vacancy:${projectId}:2"),
                    projectId = projectId,
                    title = v2.first,
                    role = v2.first.substringBefore(' ').take(20),
                    description = "Ещё одна вакансия для проекта «${s.name}». (мок-данные)",
                    requiredTags = v2.second,
                    isOpen = (idx % 4 != 0),
                    publishedAt = createdAt.plusSeconds(60L * 60L * 24L * 2L),
                )
            )
        }
    }

    private fun seedResponsesInvitesNotifications() {
        val userIds = users.map { it.userId }
        if (userIds.isEmpty()) return

        // Отклики: создаём несколько правдоподобных откликов на открытые вакансии.
        val openVacancies = vacancies.filter { it.isOpen }.take(20)
        openVacancies.forEachIndexed { idx, v ->
            val responder = userIds[(v.vacancyId.hashCode().absoluteValue + idx) % userIds.size]
            val responseId = stableUuid("response:${v.vacancyId}:$responder")
            if (responses.any { it.responseId == responseId }) return@forEachIndexed

            responses.add(
                Response(
                    responseId = responseId,
                    userId = responder,
                    vacancyId = v.vacancyId,
                    status = when (idx % 3) {
                        0 -> ResponseStatus.PENDING
                        1 -> ResponseStatus.ACCEPTED
                        else -> ResponseStatus.REJECTED
                    },
                    createdAt = baseNow.minusSeconds(60L * 60L * (2L + idx.toLong())),
                )
            )
        }

        // Приглашения: создаём входящие/исходящие сценарии для проверки UI.
        projects.take(8).forEachIndexed { idx, p ->
            val invited = userIds[(p.projectId.hashCode().absoluteValue + idx + 3) % userIds.size]
            val inviteId = stableUuid("invite:${p.projectId}:$invited")
            if (invites.any { it.inviteId == inviteId }) return@forEachIndexed
            invites.add(
                Invite(
                    inviteId = inviteId,
                    userId = invited,
                    projectId = p.projectId,
                    role = "Contributor",
                    status = when (idx % 4) {
                        0 -> InviteStatus.PENDING
                        1 -> InviteStatus.ACCEPTED
                        2 -> InviteStatus.DECLINED
                        else -> InviteStatus.CANCELLED
                    },
                    createdAt = baseNow.minusSeconds(60L * 60L * 24L * (1L + idx.toLong())),
                )
            )
        }

        // Уведомления: у текущего пользователя должно быть достаточно записей для проверки списка и badge.
        val me = currentUserId ?: UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        val extra = (1..18).map { i ->
            Notification(
                notificationId = stableUuid("notification:$me:$i"),
                userId = me,
                category = when (i % 3) {
                    0 -> NotificationCategory.RESPONSE
                    1 -> NotificationCategory.INVITATION
                    else -> NotificationCategory.PROJECT
                },
                eventType = when (i % 3) {
                    0 -> "response.${if (i % 2 == 0) "accepted" else "rejected"}"
                    1 -> "invite.created"
                    else -> "project.created"
                },
                referenceType = when (i % 3) {
                    0 -> ReferenceType.RESPONSE
                    1 -> ReferenceType.INVITATION
                    else -> ReferenceType.PROJECT
                },
                referenceId = stableUuid("ref:$i"),
                contextData = mapOf("text" to "Уведомление #$i (мок-данные)"),
                isRead = (i % 4 == 0),
                createdAt = baseNow.minusSeconds(60L * 15L * i.toLong()),
            )
        }

        extra.forEach { n ->
            if (notifications.any { it.notificationId == n.notificationId }) return@forEach
            notifications.add(0, n)
        }
    }
}
