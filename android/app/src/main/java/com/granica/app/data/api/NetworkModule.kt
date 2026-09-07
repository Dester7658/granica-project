package com.granica.app.data.api

import com.granica.app.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType

object NetworkModule {

    val baseUrl: String = BuildConfig.BASE_URL

    private val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                        else HttpLoggingInterceptor.Level.NONE
            })
            .build()
    }

    val apiService: GranicaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GranicaApiService::class.java)
    }

    /** Builds the URL the app should load (via Coil) to show the latest camera snapshot. */
    fun snapshotUrl(crossingId: String, cameraId: String): String =
        "${baseUrl}api/crossings/$crossingId/cameras/$cameraId/snapshot?ts=${System.currentTimeMillis()}"
}
