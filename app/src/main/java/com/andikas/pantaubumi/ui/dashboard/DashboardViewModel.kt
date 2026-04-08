package com.andikas.pantaubumi.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andikas.pantaubumi.domain.repository.PantauBumiRepository
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: PantauBumiRepository,
    private val fusedLocationClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    private var currentLat = -6.2297
    private var currentLng = 106.8295

    @SuppressLint("MissingPermission")
    fun fetchLocationAndLoadData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _state.update {
                    it.copy(
                        isLoading = !isRefresh,
                        isRefreshing = isRefresh
                    )
                }

                if (!hasLocationPermission()) {
                    loadData(currentLat, currentLng)
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
                loadData(currentLat, currentLng)
                _state.update { it.copy(isLoading = false, isRefreshing = false) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Gagal mendapatkan lokasi: ${e.message}",
                        isLoading = false,
                        isRefreshing = false
                    )
                }
                loadData(currentLat, currentLng)
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

    fun refresh(lat: Double, lng: Double) {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            loadData(lat, lng)
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    fun loadDashboardData(lat: Double, lng: Double) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            loadData(lat, lng)
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun loadData(lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.getRiskFlow(lat, lng).collect { riskResult ->
                _state.update { currentState ->
                    currentState.copy(
                        risk = riskResult.getOrNull() ?: currentState.risk,
                        error = if (riskResult.isFailure && currentState.risk == null) riskResult.exceptionOrNull()?.message else currentState.error,
                        lastUpdated = getCurrentTimestamp()
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.getWeatherFlow(lat, lng).collect { weatherResult ->
                _state.update { currentState ->
                    currentState.copy(
                        weather = weatherResult.getOrNull() ?: currentState.weather,
                        lastUpdated = getCurrentTimestamp()
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.getAlertsFlow(lat, lng).collect { alertsResult ->
                _state.update { currentState ->
                    currentState.copy(
                        alerts = alertsResult.getOrNull() ?: currentState.alerts,
                        lastUpdated = getCurrentTimestamp()
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.getEvacuationFlow(lat, lng).collect { evacuationResult ->
                _state.update { currentState ->
                    currentState.copy(
                        evacuations = evacuationResult.getOrNull() ?: currentState.evacuations,
                        lastUpdated = getCurrentTimestamp()
                    )
                }
            }
        }
    }

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
}
