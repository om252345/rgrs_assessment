package com.rgrs.marketplace.domain

import kotlinx.coroutines.flow.Flow

interface ListingRepository {
    fun getListings(): Flow<List<Listing>>
    suspend fun toggleFavorite(id: Long)
    suspend fun createListing(title: String, price: Double, localImagePath: String?)
}