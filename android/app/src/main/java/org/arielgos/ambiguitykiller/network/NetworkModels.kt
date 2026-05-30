package org.arielgos.ambiguitykiller.network

import com.google.gson.annotations.SerializedName

data class CloudRunRequest(
    @SerializedName("user") val user: String, @SerializedName("value") val value: String
)

data class CloudRunResponse(
    @SerializedName("user") val user: String?, @SerializedName("result") val result: String?
)
