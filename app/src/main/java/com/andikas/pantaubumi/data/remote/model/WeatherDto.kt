package com.andikas.pantaubumi.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDto(
    @SerialName("rainfall_mm_per_hour") val rainfallMmPerHour: Double,
    @SerialName("river_level_m") val riverLevelM: Double,
    @SerialName("river_level_delta_per_hour") val riverLevelDeltaPerHour: Double,
    @SerialName("latest_magnitude") val latestMagnitude: Double?,
    @SerialName("recorded_at") val recordedAt: String
)
