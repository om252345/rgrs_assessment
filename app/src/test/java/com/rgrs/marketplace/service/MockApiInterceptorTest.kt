package com.rgrs.marketplace.service

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockApiInterceptorTest {

    private val interceptor = MockApiInterceptor()
    private val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    @Test
    fun `sync endpoint returns 200 OK`() {
        val request = Request.Builder()
            .url("https://api.marketplace.rgrs.com/listings/sync")
            .post("{}".toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        assertTrue(response.body?.string()?.contains("SUCCESS") == true)
    }

    @Test
    fun `listings endpoint returns 200 OK`() {
        val request = Request.Builder()
            .url("https://api.marketplace.rgrs.com/listings")
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        assertEquals("[]", response.body?.string())
    }
}
