package com.andikas.pantaubumi.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.andikas.pantaubumi.data.local.PrefManager
import com.andikas.pantaubumi.domain.repository.PantauBumiRepository
import com.andikas.pantaubumi.worker.MapDownloadWorker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionStatus
import javax.inject.Inject
import kotlin.coroutines.resume

data class SettingsUiState(
    val notifyFlood: Boolean = true,
    val notifyLandslide: Boolean = true,
    val notifyEarthquake: Boolean = true,
    val minRiskThreshold: Int = 1,
    val mapDownloadProgress: Int? = null,
    val mapStorageSize: String = "0 MB"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefManager: PrefManager,
    private val repository: PantauBumiRepository,
    private val fusedLocationClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val size = getOfflineStorageSize(context)
            _uiState.value = _uiState.value.copy(mapStorageSize = size)

            combine(
                prefManager.notifyFlood,
                prefManager.notifyLandslide,
                prefManager.notifyEarthquake,
                prefManager.minRiskThreshold
            ) { flood, landslide, earthquake, threshold ->
                SettingsUiState(
                    notifyFlood = flood,
                    notifyLandslide = landslide,
                    notifyEarthquake = earthquake,
                    minRiskThreshold = threshold
                )
            }.collect { state ->
                val prevFlood = _uiState.value.notifyFlood
                val prevLand = _uiState.value.notifyLandslide
                val prevEarth = _uiState.value.notifyEarthquake

                _uiState.value = state.copy(
                    mapDownloadProgress = _uiState.value.mapDownloadProgress,
                    mapStorageSize = _uiState.value.mapStorageSize
                )

                checkPushNotificationState(
                    wasAllDisabled = !(prevFlood || prevLand || prevEarth),
                    isAllDisabled = !(state.notifyFlood || state.notifyLandslide || state.notifyEarthquake)
                )
            }
        }

        // Observe existing map download work persistently
        viewModelScope.launch {
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkLiveData("MapDownloadWork")
                .asFlow()
                .collect { workInfos ->
                    val workInfo = workInfos.firstOrNull()
                    if (workInfo != null) {
                        if (workInfo.state == androidx.work.WorkInfo.State.RUNNING || workInfo.state == androidx.work.WorkInfo.State.ENQUEUED) {
                            val progress = workInfo.progress.getInt("PROGRESS", 0)
                            _uiState.value = _uiState.value.copy(mapDownloadProgress = progress)
                        } else if (workInfo.state.isFinished) {
                            if (workInfo.state == androidx.work.WorkInfo.State.CANCELLED) {
                                _uiState.value = _uiState.value.copy(mapDownloadProgress = null)
                            } else if (_uiState.value.mapDownloadProgress != null) {
                                val newSize = getOfflineStorageSize(context)
                                _uiState.value = _uiState.value.copy(
                                    mapDownloadProgress = null,
                                    mapStorageSize = newSize
                                )
                            }
                        }
                    }
                }
        }
    }

    private suspend fun getOfflineStorageSize(context: Context): String =
        suspendCancellableCoroutine { continuation ->
            try {
                val offlineManager = OfflineManager.getInstance(context)
                offlineManager.listOfflineRegions(object :
                    OfflineManager.ListOfflineRegionsCallback {
                    override fun onList(offlineRegions: Array<OfflineRegion>?) {
                        if (offlineRegions.isNullOrEmpty()) {
                            if (continuation.isActive) continuation.resume("0 MB")
                            return
                        }

                        var totalSizeBytes = 0L
                        var remaining = offlineRegions.size

                        for (region in offlineRegions) {
                            region.getStatus(object : OfflineRegion.OfflineRegionStatusCallback {
                                override fun onStatus(status: OfflineRegionStatus?) {
                                    totalSizeBytes += status?.completedResourceSize ?: 0L
                                    remaining--
                                    if (remaining == 0) {
                                        val mbString = String.format(
                                            java.util.Locale.getDefault(),
                                            "%.1f MB",
                                            totalSizeBytes / (1024.0 * 1024.0)
                                        )
                                        if (continuation.isActive) continuation.resume(mbString)
                                    }
                                }

                                override fun onError(error: String?) {
                                    remaining--
                                    if (remaining == 0) {
                                        val mbString = String.format(
                                            java.util.Locale.getDefault(),
                                            "%.1f MB",
                                            totalSizeBytes / (1024.0 * 1024.0)
                                        )
                                        if (continuation.isActive) continuation.resume(mbString)
                                    }
                                }
                            })
                        }
                    }

                    override fun onError(error: String) {
                        if (continuation.isActive) continuation.resume("0 MB")
                    }
                })
            } catch (e: Exception) {
                if (continuation.isActive) continuation.resume("0 MB")
            }
        }

    private fun checkPushNotificationState(wasAllDisabled: Boolean, isAllDisabled: Boolean) {
        viewModelScope.launch {
            if (wasAllDisabled && !isAllDisabled) {
                // Re-enabled
                prefManager.fcmToken?.let { token ->
                    repository.updateFcmToken(token, prefManager.deviceId)
                }
            } else if (!wasAllDisabled && isAllDisabled) {
                // Disabled all
                prefManager.fcmToken?.let { token ->
                    repository.deleteFcmToken(token)
                }
            }
        }
    }

    fun toggleFlood(enabled: Boolean) = prefManager.setNotifyFlood(enabled)
    fun toggleLandslide(enabled: Boolean) = prefManager.setNotifyLandslide(enabled)
    fun toggleEarthquake(enabled: Boolean) = prefManager.setNotifyEarthquake(enabled)
    fun setMinRiskThreshold(threshold: Int) = prefManager.setMinRiskThreshold(threshold)

    @SuppressLint("MissingPermission")
    fun startMapDownload(context: Context) {
        viewModelScope.launch {
            try {
                val location = if (hasLocationPermission()) {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    ).await()
                } else {
                    null
                }

                val workRequest = if (location != null) {
                    val data = androidx.work.workDataOf(
                        "LAT" to location.latitude,
                        "LNG" to location.longitude
                    )
                    androidx.work.OneTimeWorkRequestBuilder<MapDownloadWorker>()
                        .setInputData(data)
                        .build()
                } else {
                    androidx.work.OneTimeWorkRequestBuilder<MapDownloadWorker>()
                        .build()
                }

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "MapDownloadWork",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cancelMapDownload(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("MapDownloadWork")
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
}
