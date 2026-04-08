package com.andikas.pantaubumi.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportDto(
    val id: Int,
    val lat: Double,
    val lng: Double,
    val text: String,
    val category: String,
    val verified: Boolean,
    @SerialName("verification_score") val verificationScore: Double,
    val source: String,
    @SerialName("flag_count") val flagCount: Int,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class CreateReportRequest(
    val lat: Double,
    val lng: Double,
    val text: String,
    val category: String? = null
)
