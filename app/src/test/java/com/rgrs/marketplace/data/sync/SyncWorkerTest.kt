package com.rgrs.marketplace.data.sync

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.rgrs.marketplace.data.ListingDao
import com.rgrs.marketplace.data.ListingEntity
import com.rgrs.marketplace.domain.SyncStatus
import com.rgrs.marketplace.service.ListingApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class SyncWorkerTest {

    private val context = mockk<Context>(relaxed = true)
    private val workerParams = mockk<WorkerParameters>(relaxed = true)
    private val listingDao = mockk<ListingDao>(relaxed = true)
    private val apiService = mockk<ListingApiService>(relaxed = true)

    private lateinit var syncWorker: SyncWorker

    @Before
    fun setUp() {
        syncWorker = SyncWorker(
            context = context,
            workerParams = workerParams,
            listingDao = listingDao,
            apiService = apiService
        )
    }

    @Test
    fun `doWork when pending listings exist and sync succeeds updates sync status to SYNCED`() = runTest {
        // Given
        val pendingListing = ListingEntity(
            id = 1L,
            title = "Test Item",
            price = 99.99,
            imageUrl = "http://example.com/image.jpg",
            localImagePath = null,
            isFavourite = false,
            createAt = 1000L,
            updatedAt = 1000L,
            syncStatus = SyncStatus.SYNC_PENDING.name
        )

        coEvery { listingDao.getPendingListings() } returns listOf(pendingListing)
        coEvery { listingDao.getAllListingsAsList() } returns listOf(pendingListing)
        coEvery { apiService.syncListings(any()) } returns Response.success(Unit)

        // When
        val result = syncWorker.doWork()

        // Then
        assertEquals(Result.success(), result)
        coVerify {
            listingDao.editListing(match {
                it.id == 1L && it.syncStatus == SyncStatus.SYNCED.name
            })
        }
    }

    @Test
    fun `doWork when no pending listings returns success`() = runTest {
        // Given
        coEvery { listingDao.getPendingListings() } returns emptyList()
        coEvery { listingDao.getAllListingsAsList() } returns emptyList()

        // When
        val result = syncWorker.doWork()

        // Then
        assertEquals(Result.success(), result)
        coVerify(exactly = 0) { apiService.syncListings(any()) }
    }

    @Test
    fun `doWork when API throws exception returns retry`() = runTest {
        // Given
        val pendingListing = ListingEntity(
            id = 1L,
            title = "Test Item",
            price = 99.99,
            imageUrl = "http://example.com/image.jpg",
            localImagePath = null,
            isFavourite = false,
            createAt = 1000L,
            updatedAt = 1000L,
            syncStatus = SyncStatus.SYNC_PENDING.name
        )

        coEvery { listingDao.getPendingListings() } returns listOf(pendingListing)
        coEvery { apiService.syncListings(any()) } throws RuntimeException("Network error")

        // When
        val result = syncWorker.doWork()

        // Then
        assertEquals(Result.retry(), result)
    }
}
