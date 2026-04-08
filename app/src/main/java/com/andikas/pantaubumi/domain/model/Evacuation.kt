package com.andikas.pantaubumi.domain.model

data class Evacuation(
    val id: Int,
    val name: String,
    val lat: Double,
    val lng: Double,
    val capacity: Int,
    val type: String,
    val address: String,
    val distanceKm: Double
)
