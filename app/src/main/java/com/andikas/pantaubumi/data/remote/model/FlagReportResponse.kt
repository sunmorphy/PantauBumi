package com.andikas.pantaubumi.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FlagReportResponse(
    @SerialName("report_id") val reportId: Int,
    @SerialName("flag_count") val flagCount: Int,
    val hidden: Boolean
)
