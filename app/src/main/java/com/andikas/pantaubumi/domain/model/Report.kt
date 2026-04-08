package com.andikas.pantaubumi.domain.model

data class Report(
    val id: Int,
    val lat: Double,
    val lng: Double,
    val text: String,
    val category: String,
    val verified: Boolean,
    val verificationScore: Double,
    val source: String,
    val flagCount: Int,
    val createdAt: String,
    val authorName: String = "Anonim",
    val imageUrl: String? = null
)
