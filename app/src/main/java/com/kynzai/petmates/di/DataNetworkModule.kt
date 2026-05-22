package com.kynzai.petmates.di

import com.kynzai.data.BuildConfig as DataBuildConfig
import com.kynzai.data.auth.AuthSessionStorage
import com.kynzai.data.network.AuthTokenProvider
import com.kynzai.data.network.EmptyAuthTokenProvider
import com.kynzai.data.network.HttpClientFactory
import com.kynzai.data.network.StorageAuthTokenProvider
import com.kynzai.data.network.SupabaseConfig
import com.kynzai.data.remote.SupabaseAuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataNetworkModule {
    @Provides
    @Singleton
    fun provideSupabaseConfig(): SupabaseConfig =
        SupabaseConfig(
            baseUrl = DataBuildConfig.SUPABASE_URL,
            anonKey = DataBuildConfig.SUPABASE_ANON_KEY,
        )

    @Provides
    @Singleton
    fun provideAuthTokenProvider(storage: AuthSessionStorage): AuthTokenProvider =
        /*
         * Токен читаем из storage, а не из SessionManager, чтобы разорвать цикл:
         * BackendApi → AuthTokenProvider → SessionManager → AuthRepository → BackendApi.
         */
        StorageAuthTokenProvider(storage)

    @Provides
    @Singleton
    fun provideHttpClient(
        config: SupabaseConfig,
        authTokenProvider: AuthTokenProvider,
    ): HttpClient =
        HttpClientFactory.create(config, authTokenProvider)

    @Provides
    @Singleton
    fun provideSupabaseAuthApi(config: SupabaseConfig): SupabaseAuthApi =
        /*
         * Auth API использует отдельный клиент без SessionManager, иначе Hilt получит цикл:
         * SessionManager -> AuthRepository -> SupabaseAuthApi -> HttpClient -> AuthTokenProvider -> SessionManager.
         */
        SupabaseAuthApi(
            http = HttpClientFactory.create(config, EmptyAuthTokenProvider),
            config = config,
        )
}
