package org.arielgos.ambiguitykiller.network

import retrofit2.http.Body
import retrofit2.http.POST

interface CloudRunService {
    @POST("your/endpoint/here")
    suspend fun executeCloudRun(@Body request: CloudRunRequest): CloudRunResponse
}
