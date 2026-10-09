package com.rgrs.marketplace.domain


enum class SyncStatus {
    SYNCED, SYNC_PENDING
}
data class Listing(
    val id: Long,
    val title: String,
    val price: Double,
    val imageUrl: String?,
    val localImagePath: String?,
    val isFavourite: Boolean,
    val createAt: Long,
    val updatedAt: Long,
    val syncStatus: SyncStatus
)