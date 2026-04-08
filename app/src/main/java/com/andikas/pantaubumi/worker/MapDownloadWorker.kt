package com.andikas.pantaubumi.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import kotlin.coroutines.resume
import kotlin.math.cos

@HiltWorker
class MapDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.Main) {
        val originalLocale = java.util.Locale.getDefault()
        java.util.Locale.setDefault(java.util.Locale("id"))

        try {
            if (!MapLibre.hasInstance()) {
                Log.e("MapDownloadWorker", "MapLibre not initialized")
                return@withContext Result.failure()
            }

            setProgress(workDataOf("PROGRESS" to 0))

            val offlineManager = OfflineManager.getInstance(applicationContext)

            val lat = inputData.getDouble("LAT", -6.200000)
            val lng = inputData.getDouble("LNG", 106.816666)

            var regionName = "Area Tersimpan"
            try {
                val geocoder =
                    android.location.Geocoder(applicationContext, java.util.Locale.getDefault())

                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                addresses?.firstOrNull()?.let { address ->
                    val name = address.locality ?: address.subAdminArea ?: address.adminArea
                    if (name != null) regionName = "Peta $name"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Calculate 30km radius bounding box
            val radiusKm = 30.0
            val latDelta = radiusKm / 111.0
            val lngDelta = radiusKm / (111.0 * cos(Math.toRadians(lat)))

            val bounds = LatLngBounds.Builder()
                .include(LatLng(lat + latDelta, lng - lngDelta))
                .include(LatLng(lat - latDelta, lng + lngDelta))
                .build()

            val definition = OfflineTilePyramidRegionDefinition(
                "https://tiles.openfreemap.org/styles/liberty",
                bounds,
                10.0,
                14.0,
                applicationContext.resources.displayMetrics.density
            )

            val metadata = regionName.toByteArray(Charsets.UTF_8)
            clearExistingOfflineRegions(offlineManager)

            suspendCancellableCoroutine { continuation ->
                offlineManager.createOfflineRegion(
                    definition,
                    metadata,
                    object : OfflineManager.CreateOfflineRegionCallback {
                        override fun onCreate(offlineRegion: OfflineRegion) {
                            offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)

                            offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                                override fun onStatusChanged(status: OfflineRegionStatus) {
                                    val percentage = if (status.requiredResourceCount > 0) {
                                        (100.0 * status.completedResourceCount / status.requiredResourceCount).toInt()
                                    } else {
                                        0
                                    }

                                    CoroutineScope(Dispatchers.IO).launch {
                                        setProgress(workDataOf("PROGRESS" to percentage))
                                    }

                                    if (status.isComplete) {
                                        Log.d("MapDownloadWorker", "Download complete!")
                                        if (continuation.isActive) continuation.resume(Result.success())
                                    }
                                }

                                override fun onError(error: OfflineRegionError) {
                                    Log.e("MapDownloadWorker", "Error: ${error.reason}")
                                    if (continuation.isActive) continuation.resume(Result.failure())
                                }

                                override fun mapboxTileCountLimitExceeded(limit: Long) {
                                    Log.e("MapDownloadWorker", "Limit exceeded: $limit")
                                    if (continuation.isActive) continuation.resume(Result.failure())
                                }
                            })
                        }

                        override fun onError(error: String) {
                            Log.e("MapDownloadWorker", "Error creating region: $error")
                            if (continuation.isActive) continuation.resume(Result.failure())
                        }
                    }
                )
            }
        } catch (e: Exception) {
            Log.e("MapDownloadWorker", "Exception downloading map", e)
            Result.failure()
        } finally {
            java.util.Locale.setDefault(originalLocale)
        }
    }

    private suspend fun clearExistingOfflineRegions(offlineManager: OfflineManager) {
        val regions = suspendCancellableCoroutine { continuation ->
            offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
                override fun onList(offlineRegions: Array<OfflineRegion>?) {
                    if (continuation.isActive) continuation.resume(offlineRegions)
                }

                override fun onError(error: String) {
                    if (continuation.isActive) continuation.resume(null)
                }
            })
        }

        regions?.forEach { region ->
            suspendCancellableCoroutine<Boolean> { continuation ->
                region.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
                    override fun onDelete() {
                        if (continuation.isActive) continuation.resume(true)
                    }

                    override fun onError(error: String) {
                        if (continuation.isActive) continuation.resume(false)
                    }
                })
            }
        }
    }
}
