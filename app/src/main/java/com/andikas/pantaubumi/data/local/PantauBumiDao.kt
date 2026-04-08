package com.andikas.pantaubumi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.andikas.pantaubumi.data.local.entity.AlertEntity
import com.andikas.pantaubumi.data.local.entity.EvacuationEntity
import com.andikas.pantaubumi.data.local.entity.RiskEntity
import com.andikas.pantaubumi.data.local.entity.WeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PantauBumiDao {

    // Risk
    @Query("SELECT * FROM risk WHERE id = 'local_risk' LIMIT 1")
    fun getRisk(): Flow<RiskEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRisk(risk: RiskEntity)

    // Weather
    @Query("SELECT * FROM weather WHERE id = 'local_weather' LIMIT 1")
    fun getWeather(): Flow<WeatherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    // Evacuations
    @Query("SELECT * FROM evacuation")
    fun getEvacuations(): Flow<List<EvacuationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvacuations(evacuations: List<EvacuationEntity>)

    @Query("DELETE FROM evacuation")
    suspend fun clearEvacuations()

    @Transaction
    suspend fun replaceEvacuations(evacuations: List<EvacuationEntity>) {
        clearEvacuations()
        insertEvacuations(evacuations)
    }

    // Alerts
    @Query("SELECT * FROM alert ORDER BY createdAt DESC")
    fun getAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<AlertEntity>)

    @Query("DELETE FROM alert")
    suspend fun clearAlerts()

    @Transaction
    suspend fun replaceAlerts(alerts: List<AlertEntity>) {
        clearAlerts()
        insertAlerts(alerts)
    }
}
