package com.rgrs.marketplace.service

import com.rgrs.marketplace.domain.Listing
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject

class MockApiInterceptor @Inject constructor() : Interceptor {
    private val mockServerListings = mutableListOf<Listing>()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // Simulate network latency (1 second)
        Thread.sleep(1000)

        return when {
            path.endsWith("/listings/sync") && request.method == "POST" -> {
                // Simulate syncing pending listings to backend
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(
                        """{"status":"SUCCESS", "syncedCount": 1}"""
                            .toResponseBody("application/json".toMediaType())
                    )
                    .build()
            }
            path.endsWith("/listings") && request.method == "GET" -> {
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("[]".toResponseBody("application/json".toMediaType()))
                    .build()
            }
            path.endsWith("/listing/edit") && request.method == "POST" -> {
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("[]".toResponseBody("application/json".toMediaType()))
                    .build()
            }
            path.endsWith("/listing/upload") && request.method == "POST" -> {
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("[]".toResponseBody("application/json".toMediaType()))
                    .build()
            }
            else -> chain.proceed(request)
        }
    }
}