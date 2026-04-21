package com.kynzai.data.di

import com.kynzai.data.BuildConfig
import com.kynzai.data.network.HttpClientFactory
import com.kynzai.data.network.SupabaseConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideSupabaseConfig(): SupabaseConfig =
        SupabaseConfig(
            baseUrl = BuildConfig.SUPABASE_URL,
            anonKey = BuildConfig.SUPABASE_ANON_KEY
        )

    @Provides
    @Singleton
    fun provideHttpClient(config: SupabaseConfig): HttpClient =
        HttpClientFactory.create(config)
}

