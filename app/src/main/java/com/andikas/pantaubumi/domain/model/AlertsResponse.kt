package com.andikas.pantaubumi.domain.model

data class AlertsResponse(
    val items: List<Alert>,
    val nextCursor: Int?,
    val hasMore: Boolean
)
