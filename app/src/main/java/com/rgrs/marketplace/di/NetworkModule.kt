package com.rgrs.marketplace.di

import com.rgrs.marketplace.service.ListingApiService
import com.rgrs.marketplace.service.MockApiInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    const val BASE_URL = "https://api.marketplace.rgrs.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(mockApiInterceptor: MockApiInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(mockApiInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideListingApiService(retrofit: Retrofit): ListingApiService {
        return retrofit
            .create(ListingApiService::class.java)
    }
}
