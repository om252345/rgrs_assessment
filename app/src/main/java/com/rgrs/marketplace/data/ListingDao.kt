package com.rgrs.marketplace.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {

    @Query("SELECT * FROM listing ORDER BY createAt DESC")
    fun getAllListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listing ORDER BY createAt DESC")
    suspend fun getAllListingsAsList(): List<ListingEntity>

    @Insert
    suspend fun insertListing(listing: ListingEntity)

    @Update
    suspend fun editListing(listing: ListingEntity)

    @Query("SELECT * FROM listing WHERE id = :id")
    fun getListingById(id: Long): ListingEntity

    @Query("SELECT * FROM listing WHERE syncStatus = 'SYNC_PENDING'")
    fun getPendingListings(): List<ListingEntity>

}