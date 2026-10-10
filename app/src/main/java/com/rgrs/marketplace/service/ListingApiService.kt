package com.rgrs.marketplace.service

import com.rgrs.marketplace.domain.Listing
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ListingApiService {
    @GET("listings")
    suspend fun getListings(): List<Listing>

    @POST("listings/sync")
    suspend fun syncListings(@Body listing: Listing): Response<Unit>

    @POST("listing/edit")
    suspend fun editListing(@Body listing: Listing): Response<Unit>

    // upload photo to endpoint in multipart
    @Multipart
    @POST("listing/upload")
    suspend fun uploadPhoto(@Part photo: MultipartBody.Part): Response<Unit>
}
