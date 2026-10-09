package com.rgrs.marketplace.di

import android.content.Context
import androidx.room3.Room
import com.rgrs.marketplace.data.AppDatabase
import com.rgrs.marketplace.data.ListingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "marketplace.db"
        ).build()
    }

    @Provides
    fun provideListingDao(database: AppDatabase): ListingDao {
        return database.listingDao
    }
}