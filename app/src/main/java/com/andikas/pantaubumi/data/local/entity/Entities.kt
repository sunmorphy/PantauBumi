package com.andikas.pantaubumi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andikas.pantaubumi.domain.model.Alert
import com.andikas.pantaubumi.domain.model.Evacuation
import com.andikas.pantaubumi.domain.model.Risk
import com.andikas.pantaubumi.domain.model.Weather

@Entity(tableName = "risk")
data class RiskEntity(
    @PrimaryKey
    val id: String = "local_risk",
    val lat: Double,
    val lng: Double,
    val floodScore: Double,
    val landslideScore: Double,
    val earthquakeScore: Double,
    val overallRisk: String,
    val computedAt: String
)

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey
    val id: String = "local_weather",
    val rainfallMmPerHour: Double,
    val riverLevelM: Double,
    val riverLevelDeltaPerHour: Double,
    val latestMagnitude: Double?,
    val recordedAt: String
)

@Entity(tableName = "evacuation")
data class EvacuationEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val lat: Double,
    val lng: Double,
    val capacity: Int,
    val type: String,
    val address: String,
    val distanceKm: Double
)

@Entity(tableName = "alert")
data class AlertEntity(
    @PrimaryKey val id: Int,
    val type: String,
    val lat: Double,
    val lng: Double,
    val severity: String,
    val message: String,
    val source: String,
    val createdAt: String
)

// Mappers
fun RiskEntity.toDomain() =
    Risk(lat, lng, floodScore, landslideScore, earthquakeScore, overallRisk, computedAt)

fun WeatherEntity.toDomain() =
    Weather(rainfallMmPerHour, riverLevelM, riverLevelDeltaPerHour, latestMagnitude, recordedAt)

fun EvacuationEntity.toDomain() =
    Evacuation(id, name, lat, lng, capacity, type, address, distanceKm)

fun AlertEntity.toDomain() = Alert(id, type, lat, lng, severity, message, source, createdAt)

fun Risk.toEntity() = RiskEntity(
    "local_risk",
    lat,
    lng,
    floodScore,
    landslideScore,
    earthquakeScore,
    overallRisk,
    computedAt
)

fun Weather.toEntity() = WeatherEntity(
    "local_weather",
    rainfallMmPerHour,
    riverLevelM,
    riverLevelDeltaPerHour,
    latestMagnitude,
    recordedAt
)

fun Evacuation.toEntity() =
    EvacuationEntity(id, name, lat, lng, capacity, type, address, distanceKm)

fun Alert.toEntity() = AlertEntity(id, type, lat, lng, severity, message, source, createdAt)
