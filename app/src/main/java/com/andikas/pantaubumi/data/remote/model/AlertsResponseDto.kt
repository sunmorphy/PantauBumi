package com.andikas.pantaubumi.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlertsResponseDto(
    val items: List<AlertDto>,
    @SerialName("next_cursor") val nextCursor: Int?,
    @SerialName("has_more") val hasMore: Boolean
)
