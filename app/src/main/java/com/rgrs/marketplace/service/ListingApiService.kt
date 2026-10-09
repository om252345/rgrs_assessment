package com.rgrs.marketplace.service

import com.rgrs.marketplace.domain.Listing
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST

interface ListingApiService {
    @GET("listings")
    suspend fun getListings(): List<Listing>

    @POST("listings/sync")
    suspend fun syncListings(listing: Listing): Response<Unit>

    @POST("listing/edit")
    suspend fun editListing(listing: Listing)

    // upload photo to endpoint in multipart
    @Multipart
    @POST("listing/upload")
    suspend fun uploadPhoto(photo: MultipartBody.Part): Response<Unit>
}