package com.andikas.pantaubumi.domain.model

data class Alert(
    val id: Int,
    val type: String,
    val lat: Double,
    val lng: Double,
    val severity: String,
    val message: String,
    val source: String,
    val createdAt: String
)
