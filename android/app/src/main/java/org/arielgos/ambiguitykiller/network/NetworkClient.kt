package org.arielgos.ambiguitykiller.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    private const val BASE_URL = "https://ambiguity-killer-api-311746519106.us-central1.run.app"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder().connectTimeout(1, TimeUnit.MINUTES).readTimeout(5, TimeUnit.MINUTES).writeTimeout(5, TimeUnit.MINUTES).addInterceptor(loggingInterceptor).build()

    private val retrofit: Retrofit = Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient).addConverterFactory(GsonConverterFactory.create()).build()

    val cloudRunService: CloudRunService = retrofit.create(CloudRunService::class.java)
}
