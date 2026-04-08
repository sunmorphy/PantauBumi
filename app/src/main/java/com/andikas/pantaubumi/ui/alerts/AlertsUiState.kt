package com.andikas.pantaubumi.ui.alerts

import androidx.paging.PagingData
import com.andikas.pantaubumi.domain.model.Alert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class AlertsUiState(
    val alerts: Flow<PagingData<Alert>> = emptyFlow(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val locationName: String = "Mencari lokasi...",
    val errorMessage: String? = null
)
