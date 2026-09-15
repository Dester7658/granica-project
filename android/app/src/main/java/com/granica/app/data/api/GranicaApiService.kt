package com.granica.app.data.api

import com.granica.app.data.model.BorderDto
import com.granica.app.data.model.BorderSummaryDto
import com.granica.app.data.model.CountryDto
import com.granica.app.data.model.CrossingDto
import com.granica.app.data.model.AiWaitEstimateDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GranicaApiService {

    @GET("api/countries")
    suspend fun getCountries(): List<CountryDto>

    @GET("api/borders")
    suspend fun getBorders(@Query("country") countryCode: String): List<BorderSummaryDto>

    @GET("api/borders/{id}")
    suspend fun getBorder(@Path("id") id: String): BorderDto

    @GET("api/crossings/{id}")
    suspend fun getCrossing(@Path("id") id: String): CrossingDto

    @GET("api/crossings/{id}/ai-estimate")
    suspend fun getAiEstimate(
        @Path("id") id: String,
        @Query("cameraId") cameraId: String
    ): AiWaitEstimateDto
}
