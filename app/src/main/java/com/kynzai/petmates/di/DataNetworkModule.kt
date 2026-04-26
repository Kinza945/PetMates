package com.kynzai.petmates.di

import com.kynzai.data.BuildConfig as DataBuildConfig
import com.kynzai.data.network.AuthTokenProvider
import com.kynzai.data.network.HttpClientFactory
import com.kynzai.data.network.SupabaseConfig
import com.kynzai.petmates.session.SessionManager
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
    fun provideAuthTokenProvider(sessionManager: SessionManager): AuthTokenProvider =
        object : AuthTokenProvider {
            override fun currentAccessToken(): String? =
                sessionManager.state.value.currentAccessToken
        }

    @Provides
    @Singleton
    fun provideHttpClient(
        config: SupabaseConfig,
        authTokenProvider: AuthTokenProvider,
    ): HttpClient =
        HttpClientFactory.create(config, authTokenProvider)
}
