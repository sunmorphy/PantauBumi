package com.andikas.pantaubumi.ui.reports

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andikas.pantaubumi.domain.repository.PantauBumiRepository
import com.andikas.pantaubumi.vo.HazardType
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: PantauBumiRepository,
    private val fusedLocationClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ReportUiState())
    val state = _state.asStateFlow()

    private var currentLat = -6.2297
    private var currentLng = 106.8295

    init {
        fetchLocationAndLoadReports()
    }

    @SuppressLint("MissingPermission")
    fun fetchLocationAndLoadReports(isRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _state.update {
                    it.copy(
                        isLoading = !isRefresh,
                        isRefreshing = isRefresh
                    )
                }

                if (!hasLocationPermission()) {
                    loadReports(isRefresh = isRefresh)
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
                loadReports(isRefresh = isRefresh)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Gagal mendapatkan lokasi: ${e.message}") }
                loadReports()
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

    fun loadReports(
        isRefresh: Boolean = false,
        hazardType: HazardType = _state.value.selectedFilter
    ) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    selectedFilter = hazardType
                )
            }

            val categoryParam = when (hazardType) {
                HazardType.ALL -> null
                else -> hazardType.label
            }

            val result = repository.getReports(
                lat = currentLat,
                lng = currentLng,
                radius = 10.0,
                category = categoryParam,
                limit = 50
            )

            result.onSuccess { reports ->
                _state.update {
                    it.copy(
                        reports = reports,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        errorMessage = e.message,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
        }
    }

    fun onFilterSelected(filter: HazardType) {
        loadReports(hazardType = filter)
    }

    fun flagReport(reportId: Int) {
        viewModelScope.launch {
            repository.flagReport(reportId).onSuccess { result ->
                _state.update { currentState ->
                    val updatedReports = currentState.reports.map { report ->
                        if (report.id == result.reportId) {
                            report.copy(flagCount = result.flagCount)
                        } else {
                            report
                        }
                    }.filter { report ->
                        !(report.id == result.reportId && result.hidden)
                    }
                    currentState.copy(reports = updatedReports)
                }
            }
        }
    }

    fun onShowAddReportDialog(show: Boolean) {
        _state.update { it.copy(showAddReportDialog = show) }
    }

    fun submitReport(text: String, category: String, imageUri: Uri?) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }

            val imageFile = imageUri?.let { uri ->
                uriToFile(context, uri)
            }

            val result = repository.createReport(
                lat = currentLat,
                lng = currentLng,
                text = text,
                category = category,
                imageFile = imageFile
            )

            result.onSuccess {
                _state.update { it.copy(isSubmitting = false, showAddReportDialog = false) }
                loadReports()
            }.onFailure { e ->
                val msg = e.message ?: ""
                if (msg.startsWith("429:")) {
                    val minutesPattern = Regex("""(\d+)""", RegexOption.IGNORE_CASE)
                    val match = minutesPattern.find(msg)
                    val minutes = match?.groupValues?.get(1)?.toIntOrNull() ?: 10
                    _state.update { it.copy(isSubmitting = false, cooldownMinutes = minutes) }
                    startCooldownTimer()
                } else {
                    _state.update { it.copy(isSubmitting = false, errorMessage = e.message) }
                }
            }

            imageFile?.delete()
        }
    }

    private var cooldownJob: kotlinx.coroutines.Job? = null

    private fun startCooldownTimer() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            while (_state.value.cooldownMinutes > 0) {
                kotlinx.coroutines.delay(60_000)
                _state.update { it.copy(cooldownMinutes = it.cooldownMinutes - 1) }
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "temp_report_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            outputStream.close()
            inputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
