package com.andikas.pantaubumi.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RiskDto(
    val lat: Double,
    val lng: Double,
    @SerialName("flood_score") val floodScore: Double,
    @SerialName("landslide_score") val landslideScore: Double,
    @SerialName("earthquake_score") val earthquakeScore: Double,
    @SerialName("overall_risk") val overallRisk: String,
    @SerialName("computed_at") val computedAt: String
)
