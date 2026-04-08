package com.andikas.pantaubumi.domain.model

data class RiskZone(
    val type: String,
    val features: List<RiskFeature>
)

data class RiskFeature(
    val type: String,
    val riskLevel: String,
    val score: Double,
    val geometry: Geometry
)

data class Geometry(
    val type: String,
    val coordinates: List<List<List<Double>>>
)

data class Route(
    val distanceMeters: Int,
    val durationSeconds: Int,
    val geometry: String,
    val steps: List<RouteStep>
)

data class RouteStep(
    val instruction: String,
    val distanceMeters: Int,
    val location: List<Double>
)
