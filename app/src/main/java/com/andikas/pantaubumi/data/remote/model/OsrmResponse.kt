package com.andikas.pantaubumi.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class OsrmResponse(
    val code: String,
    val routes: List<OsrmRoute> = emptyList()
)

@Serializable
data class OsrmRoute(
    val geometry: OsrmGeometry,
    val distance: Double,
    val duration: Double
)

@Serializable
data class OsrmGeometry(
    val coordinates: List<List<Double>>
)
