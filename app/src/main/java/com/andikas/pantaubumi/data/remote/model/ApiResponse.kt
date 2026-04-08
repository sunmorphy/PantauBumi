package com.andikas.pantaubumi.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val code: Int,
    val status: String,
    val message: String?,
    val data: T?
)
