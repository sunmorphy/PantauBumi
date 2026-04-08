package com.andikas.pantaubumi.domain.repository

import com.andikas.pantaubumi.data.remote.model.WeatherDto
import com.andikas.pantaubumi.domain.model.Alert
import com.andikas.pantaubumi.domain.model.AlertsResponse
import com.andikas.pantaubumi.domain.model.Evacuation
import com.andikas.pantaubumi.domain.model.FlagReportResult
import com.andikas.pantaubumi.domain.model.Report
import com.andikas.pantaubumi.domain.model.Risk
import com.andikas.pantaubumi.domain.model.Weather
import kotlinx.coroutines.flow.Flow
import java.io.File

interface PantauBumiRepository {
    suspend fun getRisk(lat: Double, lng: Double): Result<Risk>
    fun getRiskFlow(lat: Double, lng: Double): Flow<Result<Risk>>

    suspend fun getWeather(lat: Double, lng: Double): Result<WeatherDto>
    fun getWeatherFlow(lat: Double, lng: Double): Flow<Result<Weather>>

    suspend fun getAlerts(
        lat: Double,
        lng: Double,
        radiusKm: Double? = null,
        hours: Int? = null,
        limit: Int? = null,
        beforeId: Int? = null
    ): Result<AlertsResponse>

    fun getAlertsFlow(lat: Double, lng: Double): Flow<Result<List<Alert>>>

    suspend fun getEvacuation(lat: Double, lng: Double, limit: Int?): Result<List<Evacuation>>
    fun getEvacuationFlow(lat: Double, lng: Double): Flow<Result<List<Evacuation>>>

    suspend fun getReports(
        lat: Double,
        lng: Double,
        radius: Double? = null,
        category: String? = null,
        limit: Int? = null
    ): Result<List<Report>>

    suspend fun createReport(
        lat: Double,
        lng: Double,
        text: String,
        category: String?,
        imageFile: File? = null
    ): Result<Report>

    suspend fun flagReport(reportId: Int): Result<FlagReportResult>
    suspend fun updateFcmToken(token: String, deviceId: String?): Result<Unit>
    suspend fun deleteFcmToken(token: String): Result<Unit>

    // Map Related
    suspend fun getEvacuationPoints(
        lat: Double,
        lng: Double,
    ): Result<List<Evacuation>>

    suspend fun getEvacuationById(id: Int): Evacuation?

    suspend fun getReportsMap(lat: Double, lng: Double, radius: Double): Result<List<Report>>

    suspend fun getRoute(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): Result<com.andikas.pantaubumi.domain.model.Route>
}
