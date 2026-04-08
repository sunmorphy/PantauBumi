package com.andikas.pantaubumi.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.andikas.pantaubumi.R
import com.andikas.pantaubumi.data.local.PrefManager
import com.andikas.pantaubumi.domain.repository.PantauBumiRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PantauBumiMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var repository: PantauBumiRepository

    @Inject
    lateinit var prefManager: PrefManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            prefManager.setFcmToken(token)
            repository.updateFcmToken(token, prefManager.deviceId)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "Peringatan Bencana"
        val body = message.notification?.body ?: message.data["body"]
        ?: "Harap waspada dan periksa aplikasi untuk detail lebih lanjut."

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pantaubumi_alerts",
                "PantauBumi Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "pantaubumi_alerts")
            .setSmallIcon(R.drawable.ic_crisis_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
