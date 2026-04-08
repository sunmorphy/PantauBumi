package com.andikas.pantaubumi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.andikas.pantaubumi.data.local.entity.AlertEntity
import com.andikas.pantaubumi.data.local.entity.EvacuationEntity
import com.andikas.pantaubumi.data.local.entity.RiskEntity
import com.andikas.pantaubumi.data.local.entity.WeatherEntity

@Database(
    entities = [
        RiskEntity::class,
        WeatherEntity::class,
        EvacuationEntity::class,
        AlertEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PantauBumiDatabase : RoomDatabase() {
    abstract val dao: PantauBumiDao

    companion object {
        const val DATABASE_NAME = "pantaubumi_db"
    }
}
