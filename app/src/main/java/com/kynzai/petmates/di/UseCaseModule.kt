package com.kynzai.petmates.di

import com.kynzai.domain.repositories.InviteRepository
import com.kynzai.domain.repositories.NotificationRepository
import com.kynzai.domain.repositories.ProjectRepository
import com.kynzai.domain.repositories.ResponseRepository
import com.kynzai.domain.repositories.UserRepository
import com.kynzai.domain.repositories.VacancyRepository
import com.kynzai.domain.usecases.CreateInviteUseCase
import com.kynzai.domain.usecases.CreateProjectUseCase
import com.kynzai.domain.usecases.CreateVacancyUseCase
import com.kynzai.domain.usecases.GetFeedProjectsUseCase
import com.kynzai.domain.usecases.GetNotificationsUseCase
import com.kynzai.domain.usecases.GetProjectDetailsUseCase
import com.kynzai.domain.usecases.RespondToVacancyUseCase
import com.kynzai.domain.usecases.SearchUsersUseCase
import com.kynzai.domain.usecases.UpdateResponseStatusUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    @Singleton
    fun provideCreateProjectUseCase(projects: ProjectRepository): CreateProjectUseCase =
        CreateProjectUseCase(projects)

    @Provides
    @Singleton
    fun provideCreateVacancyUseCase(vacancies: VacancyRepository): CreateVacancyUseCase =
        CreateVacancyUseCase(vacancies)

    @Provides
    @Singleton
    fun provideCreateInviteUseCase(invites: InviteRepository): CreateInviteUseCase =
        CreateInviteUseCase(invites)

    @Provides
    @Singleton
    fun provideSearchUsersUseCase(users: UserRepository): SearchUsersUseCase =
        SearchUsersUseCase(users)

    @Provides
    @Singleton
    fun provideGetFeedProjectsUseCase(projects: ProjectRepository): GetFeedProjectsUseCase =
        GetFeedProjectsUseCase(projects)

    @Provides
    @Singleton
    fun provideGetProjectDetailsUseCase(
        projects: ProjectRepository,
        users: UserRepository,
    ): GetProjectDetailsUseCase =
        GetProjectDetailsUseCase(projects, users)

    @Provides
    @Singleton
    fun provideRespondToVacancyUseCase(responses: ResponseRepository): RespondToVacancyUseCase =
        RespondToVacancyUseCase(responses)

    @Provides
    @Singleton
    fun provideUpdateResponseStatusUseCase(responses: ResponseRepository): UpdateResponseStatusUseCase =
        UpdateResponseStatusUseCase(responses)

    @Provides
    @Singleton
    fun provideGetNotificationsUseCase(notifications: NotificationRepository): GetNotificationsUseCase =
        GetNotificationsUseCase(notifications)
}
