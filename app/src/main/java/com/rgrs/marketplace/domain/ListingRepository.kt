package com.rgrs.marketplace.domain

import kotlinx.coroutines.flow.Flow

interface ListingRepository {
    fun getListings(): Flow<List<Listing>>
    fun hasPendingListings(): Flow<Boolean>
    suspend fun toggleFavorite(id: Long)
    suspend fun createListing(title: String, price: Double, localImagePath: String?)
    fun syncPendingListings()
}
