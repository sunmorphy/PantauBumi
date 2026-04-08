package com.andikas.pantaubumi.ui.alerts

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.andikas.pantaubumi.data.paging.AlertPagingSource
import com.andikas.pantaubumi.data.remote.PantauBumiApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val api: PantauBumiApi,
    private val fusedLocationClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(AlertsUiState())
    val state: StateFlow<AlertsUiState> = _state.asStateFlow()

    private var currentLat = -6.2297
    private var currentLng = 106.8295

    init {
        fetchLocationAndLoadAlerts()
    }

    @SuppressLint("MissingPermission")
    fun fetchLocationAndLoadAlerts(isRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _state.update {
                    it.copy(
                        isLoading = !isRefresh,
                        isRefreshing = isRefresh
                    )
                }

                if (!hasLocationPermission()) {
                    loadAlerts(currentLat, currentLng)
                    _state.update { it.copy(isLoading = false, isRefreshing = false) }
                    return@launch
                }

                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()

                location?.let {
                    currentLat = it.latitude
                    currentLng = it.longitude
                    updateLocationName(it.latitude, it.longitude)
                }
                loadAlerts(currentLat, currentLng)
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        errorMessage = "Gagal mendapatkan lokasi: ${e.message}",
                        isLoading = false,
                        isRefreshing = false
                    )
                }
                loadAlerts(currentLat, currentLng)
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocationGranted || coarseLocationGranted
    }

    private suspend fun updateLocationName(lat: Double, lng: Double) {
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                val cityName = addresses?.firstOrNull()?.let { address ->
                    address.locality ?: address.subAdminArea ?: address.adminArea
                } ?: "Lokasi Tidak Diketahui"

                _state.update { it.copy(locationName = cityName) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadAlerts(lat: Double, lng: Double) {
        val alertsFlow = Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { AlertPagingSource(api, lat, lng) }
        ).flow.cachedIn(viewModelScope)

        _state.update { it.copy(alerts = alertsFlow) }
    }
}
