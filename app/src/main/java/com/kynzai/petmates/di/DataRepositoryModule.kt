package com.kynzai.petmates.di

import android.content.Context
import com.kynzai.data.BuildConfig as DataBuildConfig
import com.kynzai.data.auth.EncryptedAuthSessionStorage
import com.kynzai.data.auth.SharedPreferencesAuthSessionStorage
import com.kynzai.data.mock.FakeDataSource
import com.kynzai.data.remote.SupabaseAuthApi
import com.kynzai.data.remote.SupabaseRestApi
import com.kynzai.data.repositories.AuthRepositoryImpl
import com.kynzai.data.repositories.InviteRepositoryImpl
import com.kynzai.data.repositories.NotificationRepositoryImpl
import com.kynzai.data.repositories.ProjectRepositoryImpl
import com.kynzai.data.repositories.ResponseRepositoryImpl
import com.kynzai.data.repositories.UserRepositoryImpl
import com.kynzai.data.repositories.VacancyRepositoryImpl
import com.kynzai.data.repositories.mock.MockAuthRepository
import com.kynzai.data.repositories.mock.MockInviteRepository
import com.kynzai.data.repositories.mock.MockNotificationRepository
import com.kynzai.data.repositories.mock.MockProjectRepository
import com.kynzai.data.repositories.mock.MockResponseRepository
import com.kynzai.data.repositories.mock.MockUserRepository
import com.kynzai.data.repositories.mock.MockVacancyRepository
import com.kynzai.domain.repositories.AuthRepository
import com.kynzai.domain.repositories.InviteRepository
import com.kynzai.domain.repositories.NotificationRepository
import com.kynzai.domain.repositories.ProjectRepository
import com.kynzai.domain.repositories.ResponseRepository
import com.kynzai.domain.repositories.UserRepository
import com.kynzai.domain.repositories.VacancyRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataRepositoryModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context,
        fake: FakeDataSource,
        authApi: SupabaseAuthApi,
    ): AuthRepository {
        val prefs = context.getSharedPreferences("petmates_auth", Context.MODE_PRIVATE)
        return if (DataBuildConfig.USE_MOCKS) {
            MockAuthRepository(fake, SharedPreferencesAuthSessionStorage(prefs))
        } else {
            AuthRepositoryImpl(authApi, EncryptedAuthSessionStorage(prefs))
        }
    }

    @Provides
    @Singleton
    fun provideProjectRepository(
        api: SupabaseRestApi,
        fake: FakeDataSource,
    ): ProjectRepository =
        if (DataBuildConfig.USE_MOCKS) MockProjectRepository(fake) else ProjectRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideUserRepository(
        api: SupabaseRestApi,
        fake: FakeDataSource,
    ): UserRepository =
        if (DataBuildConfig.USE_MOCKS) MockUserRepository(fake) else UserRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideVacancyRepository(
        api: SupabaseRestApi,
        fake: FakeDataSource,
    ): VacancyRepository =
        if (DataBuildConfig.USE_MOCKS) MockVacancyRepository(fake) else VacancyRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideResponseRepository(
        api: SupabaseRestApi,
        fake: FakeDataSource,
    ): ResponseRepository =
        if (DataBuildConfig.USE_MOCKS) MockResponseRepository(fake) else ResponseRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideInviteRepository(
        api: SupabaseRestApi,
        fake: FakeDataSource,
    ): InviteRepository =
        if (DataBuildConfig.USE_MOCKS) MockInviteRepository(fake) else InviteRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideNotificationRepository(
        api: SupabaseRestApi,
        fake: FakeDataSource,
    ): NotificationRepository =
        if (DataBuildConfig.USE_MOCKS) MockNotificationRepository(fake) else NotificationRepositoryImpl(api)
}
