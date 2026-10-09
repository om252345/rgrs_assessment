package com.rgrs.marketplace.di

import android.content.Context
import androidx.room3.Room
import com.rgrs.marketplace.data.AppDatabase
import com.rgrs.marketplace.data.ListingDao
import com.rgrs.marketplace.data.ListingRepositoryImpl
import com.rgrs.marketplace.domain.ListingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindListingRepository(
        impl: ListingRepositoryImpl
    ): ListingRepository
}


