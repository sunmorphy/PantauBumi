package com.andikas.pantaubumi.domain.model

data class Risk(
    val lat: Double,
    val lng: Double,
    val floodScore: Double,
    val landslideScore: Double,
    val earthquakeScore: Double,
    val overallRisk: String,
    val computedAt: String
)
