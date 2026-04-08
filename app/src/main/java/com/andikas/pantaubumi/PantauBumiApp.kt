package com.andikas.pantaubumi

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.andikas.pantaubumi.worker.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import org.maplibre.android.MapLibre
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class PantauBumiApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // Ensure necessary directories exist before MapLibre touches them 
        // to prevent DatabaseFileSource sqlite No Such File exceptions in background processes
        val fDir = this.filesDir
        if (!fDir.exists()) fDir.mkdirs()
        
        val cDir = this.cacheDir
        if (!cDir.exists()) cDir.mkdirs()

        val defaultLocale = Locale.getDefault()
        Locale.setDefault(Locale("id"))
        
        try {
            MapLibre.getInstance(this)
        } finally {
            Locale.setDefault(defaultLocale)
        }

        SyncWorker.startPeriodicSync(this)
    }
}
