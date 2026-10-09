package com.rgrs.marketplace.data

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(entities = [ListingEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract val listingDao: ListingDao
}