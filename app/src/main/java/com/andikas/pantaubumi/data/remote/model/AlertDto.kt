package com.andikas.pantaubumi.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlertDto(
    val id: Int,
    val type: String,
    val lat: Double,
    val lng: Double,
    val severity: String,
    val message: String,
    val source: String,
    @SerialName("created_at") val createdAt: String
)
