package com.andikas.pantaubumi.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.andikas.pantaubumi.R
import com.andikas.pantaubumi.domain.repository.PantauBumiRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: PantauBumiRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val lat = inputData.getDouble("lat", -6.2)
        val lng = inputData.getDouble("lng", 106.8)

        return try {
            val riskResult = repository.getRisk(lat, lng)
            repository.getAlerts(lat, lng, 100.0, 24)
            repository.getEvacuation(lat, lng, 5)

            val risk = riskResult.getOrNull()
            if (risk != null) {
                val riskLevel = risk.overallRisk.lowercase()
                if (riskLevel == "high" || riskLevel == "critical") {
                    showNotification(riskLevel)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(riskLevel: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pantaubumi_alerts",
                "PantauBumi Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notification = NotificationCompat.Builder(applicationContext, "pantaubumi_alerts")
            .setSmallIcon(R.drawable.ic_crisis_alert)
            .setContentTitle("Peringatan Bencana!")
            .setContentText("Tingkat risiko: ${riskLevel.uppercase()}. Tetap waspada.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(riskLevel.hashCode(), notification)
    }

    companion object {
        private const val SYNC_WORK_NAME = "SyncWorkName"

        fun startPeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
