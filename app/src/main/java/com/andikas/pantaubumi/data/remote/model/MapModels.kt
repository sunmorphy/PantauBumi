package com.andikas.pantaubumi.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RiskZoneDto(
    val type: String,
    val features: List<RiskFeatureDto>
)

@Serializable
data class RiskFeatureDto(
    val type: String,
    val properties: RiskPropertiesDto,
    val geometry: GeometryDto
)

@Serializable
data class RiskPropertiesDto(
    @SerialName("risk_level") val riskLevel: String,
    val score: Double
)

@Serializable
data class GeometryDto(
    val type: String,
    val coordinates: List<List<List<Double>>>
)

@Serializable
data class RouteDto(
    @SerialName("distance_meters") val distanceMeters: Int,
    @SerialName("duration_seconds") val durationSeconds: Int,
    val geometry: String,
    val steps: List<RouteStepDto>
)

@Serializable
data class RouteStepDto(
    val instruction: String,
    @SerialName("distance_meters") val distanceMeters: Int,
    val location: List<Double>
)
