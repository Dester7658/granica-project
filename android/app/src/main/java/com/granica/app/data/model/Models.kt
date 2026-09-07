package com.granica.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CountryDto(
    val code: String,
    val name: String
)

@Serializable
data class BorderSummaryDto(
    val id: String,
    val otherCountryCode: String,
    val otherCountryName: String,
    val crossingCount: Int
)

@Serializable
data class BorderDto(
    val id: String,
    val countryA: String,
    val countryB: String,
    val crossings: List<CrossingDto> = emptyList()
)

@Serializable
data class CameraDto(
    val id: String,
    val name: String,
    val type: String,
    val snapshotUrl: String? = null,
    val streamUrl: String? = null,
    val pageUrl: String? = null,
    val refreshSeconds: Int? = null
)

@Serializable
data class CrossingDto(
    val id: String,
    val name: String,
    @SerialName("isDemo") val isDemo: Boolean = false,
    val lat: Double,
    val lon: Double,
    val direction: String,
    val cameras: List<CameraDto> = emptyList(),
    val waitMinutes: Int? = null,
    val waitUpdatedAt: String? = null,
    val source: String? = null
)
