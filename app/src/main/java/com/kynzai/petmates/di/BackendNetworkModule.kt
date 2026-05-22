package com.kynzai.petmates.di

import com.kynzai.data.BuildConfig as DataBuildConfig
import com.kynzai.data.network.AuthTokenProvider
import com.kynzai.data.network.BackendConfig
import com.kynzai.data.network.BackendHttpClientFactory
import com.kynzai.data.network.EmptyAuthTokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackendNetworkModule {
    @Provides
    @Singleton
    fun provideBackendConfig(): BackendConfig =
        BackendConfig(baseUrl = DataBuildConfig.API_BASE_URL)

    @Provides
    @Singleton
    @Named("backend")
    fun provideBackendHttpClient(authTokenProvider: AuthTokenProvider): HttpClient =
        BackendHttpClientFactory.create(authTokenProvider)

    @Provides
    @Singleton
    @Named("backendAuth")
    fun provideBackendAuthHttpClient(): HttpClient =
        BackendHttpClientFactory.create(EmptyAuthTokenProvider)

}
