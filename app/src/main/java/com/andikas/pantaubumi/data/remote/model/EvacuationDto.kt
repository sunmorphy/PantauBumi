package com.andikas.pantaubumi.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EvacuationDto(
    val id: Int,
    val name: String,
    val lat: Double,
    val lng: Double,
    val capacity: Int,
    val type: String,
    val address: String,
    @SerialName("distance_km") val distanceKm: Double
)
