package com.andikas.pantaubumi.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andikas.pantaubumi.domain.model.Evacuation
import com.andikas.pantaubumi.domain.repository.PantauBumiRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
class MapViewModel @Inject constructor(
    private val repository: PantauBumiRepository,
    private val fusedLocationClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        fetchLocation()
    }

    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        viewModelScope.launch {
            try {
                if (!hasLocationPermission()) return@launch

                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()

                location?.let {
                    _state.update { state ->
                        state.copy(
                            userLat = it.latitude,
                            userLng = it.longitude
                        )
                    }
                    updateLocationName(it.latitude, it.longitude)

                    val selected = _state.value.selectedEvacuation
                    if (selected != null) {
                        onEvacuationSelected(selected)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

    fun onViewportChanged(lat: Double, lng: Double, radius: Double) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            loadMapData(lat, lng, radius)
        }
    }

    private fun loadMapData(lat: Double, lng: Double, radius: Double) {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val evacuationResult = repository.getEvacuationPoints(lat, lng)
            evacuationResult.onSuccess { points ->
                _state.update { it.copy(evacuationPoints = points) }
            }
        }

        viewModelScope.launch {
            val reportsResult = repository.getReportsMap(lat, lng, radius)
            reportsResult.onSuccess { reports ->
                _state.update { it.copy(reports = reports) }
            }
        }

        _state.update { it.copy(isLoading = false) }
    }

    fun onEvacuationSelected(evacuation: Evacuation?) {
        _state.update {
            it.copy(
                selectedEvacuation = evacuation,
                route = null,
                isLoading = evacuation != null
            )
        }

        if (evacuation != null) {
            val userLat = _state.value.userLat ?: return
            val userLng = _state.value.userLng ?: return
            viewModelScope.launch {
                val routeResult =
                    repository.getRoute(userLat, userLng, evacuation.lat, evacuation.lng)
                routeResult.onSuccess { route ->
                    _state.update { it.copy(route = route, isLoading = false) }
                }.onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Gagal mengambil rute: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    fun loadTargetEvacuation(id: Int) {
        viewModelScope.launch {
            val target = repository.getEvacuationById(id)
            if (target != null) {
                val currentPoints = _state.value.evacuationPoints
                if (currentPoints.find { it.id == id } == null) {
                    _state.update { it.copy(evacuationPoints = currentPoints + target) }
                }
                onEvacuationSelected(target)
            }
        }
    }
}
