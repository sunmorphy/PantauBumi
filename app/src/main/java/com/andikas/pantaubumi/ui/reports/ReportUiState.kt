package com.andikas.pantaubumi.ui.reports

import com.andikas.pantaubumi.domain.model.Report
import com.andikas.pantaubumi.vo.HazardType

data class ReportUiState(
    val reports: List<Report> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val selectedFilter: HazardType = HazardType.ALL,
    val locationName: String = "Jakarta Selatan",
    val showAddReportDialog: Boolean = false,
    val cooldownMinutes: Int = 0
)