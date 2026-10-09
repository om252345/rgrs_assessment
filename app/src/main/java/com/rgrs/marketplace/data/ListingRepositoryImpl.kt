package com.rgrs.marketplace.data

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rgrs.marketplace.data.sync.SyncWorker
import com.rgrs.marketplace.domain.Listing
import com.rgrs.marketplace.domain.ListingRepository
import com.rgrs.marketplace.domain.SyncStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

class ListingRepositoryImpl @Inject constructor(
    private val listingDao: ListingDao,
    @param:ApplicationContext private val context: Context
) : ListingRepository {
    override fun getListings(): Flow<List<Listing>> {
        return listingDao.getAllListings().map { it.map { listing -> listing.toDomainModel() } }
    }

    override suspend fun toggleFavorite(id: Long) {
        withContext(Dispatchers.IO) {
            val listing = listingDao.getListingById(id)
            listingDao.editListing(listing.copy(
                isFavourite = !listing.isFavourite,
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.SYNC_PENDING.name)
            )
            enqueueSyncWork()
        }
    }

    override suspend fun createListing(
        title: String,
        price: Double,
        localImagePath: String?
    ) {
        withContext(Dispatchers.IO) {
            val savedPath = saveLocalImage(localImagePath)
            val listing = ListingEntity(
                title = title,
                price = price,
                imageUrl = null,
                localImagePath = savedPath,
                isFavourite = false,
                createAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.SYNC_PENDING.name
            )
            listingDao.insertListing(listing)
            enqueueSyncWork()
        }
    }

    private fun saveLocalImage(imageUriString: String?): String? {
        if (imageUriString.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(imageUriString)
            if (uri.scheme == "content" || uri.scheme == "file") {
                val imagesDir = File(context.filesDir, "listing_images").apply { mkdirs() }
                val imageFile = File(imagesDir, "img_${UUID.randomUUID()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    imageFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                imageFile.absolutePath
            } else {
                imageUriString
            }
        } catch (_: Exception) {
            imageUriString
        }
    }

    private fun enqueueSyncWork() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "SyncWorker",
            ExistingWorkPolicy.KEEP,
            syncRequest
        )
    }
}