package com.rgrs.marketplace.data.sync

import android.content.Context
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


@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val listingDao: ListingDao,
    private val apiService: ListingApiService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch pending listings from Room
            val pendingListings = listingDao.getPendingListings()

            for (listing in pendingListings) {
                // 2. Post to Mock REST API
                val response = apiService.syncListings(listing.toDomainModel())
                if (response.isSuccessful) {
                    // 3. Update status to SYNCED
                    listingDao.editListing(listing.copy(syncStatus = SyncStatus.SYNCED.name))
                }
            }
            // upload photos if listing imageUrl is null
            val listings = listingDao.getAllListingsAsList()
            for (listing in listings) {
                if (listing.imageUrl == null && listing.localImagePath != null) {
                    val multiPartPhoto = prepareMultiPartPhoto(applicationContext, listing.localImagePath)
                    val response = apiService.uploadPhoto(multiPartPhoto)
                    if (response.isSuccessful) {
                        // 3. Update status to SYNCED
                        listingDao.editListing(listing.copy(syncStatus = SyncStatus.SYNCED.name))
                        // 4. delete photo from app storage
                        applicationContext.deleteFile(listing.localImagePath)
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun prepareMultiPartPhoto(
        applicationContext: Context,
        localImagePath: String
    ): MultipartBody.Part {
        val file = applicationContext.getFileStreamPath(localImagePath)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("photo", file.name, requestFile)

    }
}