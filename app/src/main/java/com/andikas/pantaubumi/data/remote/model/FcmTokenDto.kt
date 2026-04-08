package com.andikas.pantaubumi.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FcmTokenDto(
    val id: Int? = null,
    val token: String,
    @SerialName("device_id") val deviceId: String? = null
)
