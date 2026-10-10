package com.rgrs.marketplace.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rgrs.marketplace.data.ListingDao
import com.rgrs.marketplace.domain.SyncStatus
import com.rgrs.marketplace.service.ListingApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val listingDao: ListingDao,
    private val apiService: ListingApiService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Fetch pending listings from Room database
            val pendingListings = listingDao.getPendingListings()

            for (listing in pendingListings) {
                try {
                    var updatedListing = listing

                    // Upload local image photo if present
                    if (!listing.localImagePath.isNullOrBlank()) {
                        val file = File(listing.localImagePath)
                        if (file.exists()) {
                            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                            val multiPartPhoto =
                                MultipartBody.Part.createFormData("photo", file.name, requestFile)
                            val photoResponse = apiService.uploadPhoto(multiPartPhoto)
                            if (photoResponse.isSuccessful) {
                                updatedListing = updatedListing.copy(
                                    imageUrl = "https://api.marketplace.rgrs.com/photos/${file.name}"
                                )
                            }
                        }
                    }

                    // Sync listing to REST API
                    val response = apiService.syncListings(updatedListing.toDomainModel())
                    if (response.isSuccessful) {
                        // Update status to SYNCED in Room database
                        listingDao.editListing(
                            updatedListing.copy(syncStatus = SyncStatus.SYNCED.name)
                        )
                    }
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Error syncing listing: ${e.message}")
                }
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
