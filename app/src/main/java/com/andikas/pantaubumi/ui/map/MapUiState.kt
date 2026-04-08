package com.andikas.pantaubumi.ui.map

import com.andikas.pantaubumi.domain.model.Evacuation
import com.andikas.pantaubumi.domain.model.Report
import com.andikas.pantaubumi.domain.model.RiskZone
import com.andikas.pantaubumi.domain.model.Route
import com.andikas.pantaubumi.vo.HazardType

data class MapUiState(
    val userLat: Double? = null,
    val userLng: Double? = null,
    val riskZones: RiskZone? = null,
    val evacuationPoints: List<Evacuation> = emptyList(),
    val reports: List<Report> = emptyList(),
    val selectedEvacuation: Evacuation? = null,
    val route: Route? = null,
    val locationName: String = "Mencari lokasi...",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isOfflineMode: Boolean = false
)