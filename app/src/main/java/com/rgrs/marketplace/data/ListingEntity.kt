package com.rgrs.marketplace.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.rgrs.marketplace.domain.Listing
import com.rgrs.marketplace.domain.SyncStatus

@Entity(tableName = "listing")
data class ListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val price: Double,
    val imageUrl: String?,
    val localImagePath: String?,
    val isFavourite: Boolean,
    val createAt: Long,
    val updatedAt: Long,
    val syncStatus: String
) {
    fun toDomainModel() = Listing(
        this.id,
        this.title,
        this.price,
        this.imageUrl,
        this.localImagePath,
        this.isFavourite,
        this.createAt,
        this.updatedAt,
        SyncStatus.valueOf(this.syncStatus)
    )
}