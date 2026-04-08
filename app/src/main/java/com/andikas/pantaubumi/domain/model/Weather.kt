package com.andikas.pantaubumi.domain.model

data class Weather(
    val rainfallMmPerHour: Double,
    val riverLevelM: Double,
    val riverLevelDeltaPerHour: Double,
    val latestMagnitude: Double?,
    val recordedAt: String
)
