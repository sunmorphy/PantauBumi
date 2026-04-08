package com.andikas.pantaubumi.ui.dashboard

import com.andikas.pantaubumi.domain.model.Alert
import com.andikas.pantaubumi.domain.model.Evacuation
import com.andikas.pantaubumi.domain.model.Risk
import com.andikas.pantaubumi.domain.model.Weather

data class DashboardUiState(
    val isLoading: Boolean = false,
    val risk: Risk? = null,
    val weather: Weather? = null,
    val alerts: List<Alert> = emptyList(),
    val evacuations: List<Evacuation> = emptyList(),
    val locationName: String = "Mencari lokasi...",
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val lastUpdated: String? = null
)
