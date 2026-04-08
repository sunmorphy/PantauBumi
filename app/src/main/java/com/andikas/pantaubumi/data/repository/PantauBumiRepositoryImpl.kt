package com.andikas.pantaubumi.data.repository

import com.andikas.pantaubumi.data.local.PantauBumiDao
import com.andikas.pantaubumi.data.local.PrefManager
import com.andikas.pantaubumi.data.local.entity.toDomain
import com.andikas.pantaubumi.data.local.entity.toEntity
import com.andikas.pantaubumi.data.mapper.toDomain
import com.andikas.pantaubumi.data.remote.OsrmApi
import com.andikas.pantaubumi.data.remote.PantauBumiApi
import com.andikas.pantaubumi.data.remote.model.FcmTokenDto
import com.andikas.pantaubumi.data.remote.model.WeatherDto
import com.andikas.pantaubumi.domain.model.Alert
import com.andikas.pantaubumi.domain.model.AlertsResponse
import com.andikas.pantaubumi.domain.model.Evacuation
import com.andikas.pantaubumi.domain.model.FlagReportResult
import com.andikas.pantaubumi.domain.model.Report
import com.andikas.pantaubumi.domain.model.Risk
import com.andikas.pantaubumi.domain.model.Weather
import com.andikas.pantaubumi.domain.repository.PantauBumiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PantauBumiRepositoryImpl @Inject constructor(
    private val api: PantauBumiApi,
    private val osrmApi: OsrmApi,
    private val prefManager: PrefManager,
    private val dao: PantauBumiDao
) : PantauBumiRepository {

    override suspend fun getRisk(lat: Double, lng: Double): Result<Risk> {
        return try {
            val response = api.getRisk(lat, lng)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getRiskFlow(lat: Double, lng: Double): Flow<Result<Risk>> = flow {
        val local = dao.getRisk().firstOrNull()
        if (local != null) {
            emit(Result.success(local.toDomain()))
        }
        val remoteResult = getRisk(lat, lng)
        if (remoteResult.isSuccess) {
            val data = remoteResult.getOrThrow()
            dao.insertRisk(data.toEntity())
            emit(Result.success(data))
        } else {
            emit(Result.failure(remoteResult.exceptionOrNull()!!))
        }
    }

    override suspend fun getWeather(lat: Double, lng: Double): Result<WeatherDto> {
        return try {
            val response = api.getWeather(lat, lng)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getWeatherFlow(lat: Double, lng: Double): Flow<Result<Weather>> = flow {
        val local = dao.getWeather().firstOrNull()
        if (local != null) {
            emit(Result.success(local.toDomain()))
        }
        val remoteResult = getWeather(lat, lng)
        if (remoteResult.isSuccess) {
            val dto = remoteResult.getOrThrow()
            val domain = Weather(
                dto.rainfallMmPerHour,
                dto.riverLevelM,
                dto.riverLevelDeltaPerHour,
                dto.latestMagnitude,
                dto.recordedAt
            )
            dao.insertWeather(domain.toEntity())
            emit(Result.success(domain))
        } else {
            emit(Result.failure(remoteResult.exceptionOrNull()!!))
        }
    }

    override suspend fun getAlerts(
        lat: Double,
        lng: Double,
        radiusKm: Double?,
        hours: Int?,
        limit: Int?,
        beforeId: Int?
    ): Result<AlertsResponse> {
        return try {
            val response = api.getAlerts(lat, lng, radiusKm, hours, limit, beforeId)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAlertsFlow(lat: Double, lng: Double): Flow<Result<List<Alert>>> = flow {
        val local = dao.getAlerts().firstOrNull()
        if (!local.isNullOrEmpty()) {
            emit(Result.success(local.map { it.toDomain() }))
        }
        val remoteResult = getAlerts(lat, lng, limit = 3)
        if (remoteResult.isSuccess) {
            val data = remoteResult.getOrThrow().items
            dao.replaceAlerts(data.map { it.toEntity() })
            emit(Result.success(data))
        } else {
            emit(Result.failure(remoteResult.exceptionOrNull()!!))
        }
    }

    override suspend fun getEvacuation(
        lat: Double,
        lng: Double,
        limit: Int?
    ): Result<List<Evacuation>> {
        return try {
            val response = api.getEvacuation(lat, lng, limit)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data.map { it.toDomain() })
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getEvacuationFlow(lat: Double, lng: Double): Flow<Result<List<Evacuation>>> =
        flow {
            val local = dao.getEvacuations().firstOrNull()
            if (!local.isNullOrEmpty()) {
                emit(Result.success(local.map { it.toDomain() }))
            }
            val remoteResult = getEvacuation(lat, lng, 1)
            if (remoteResult.isSuccess) {
                val data = remoteResult.getOrThrow()
                dao.replaceEvacuations(data.map { it.toEntity() })
                emit(Result.success(data))
            } else {
                emit(Result.failure(remoteResult.exceptionOrNull()!!))
            }
        }

    override suspend fun getReports(
        lat: Double,
        lng: Double,
        radius: Double?,
        category: String?,
        limit: Int?
    ): Result<List<Report>> {
        return try {
            val response = api.getReports(lat, lng, radius, category, limit)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data.map { it.toDomain() })
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createReport(
        lat: Double,
        lng: Double,
        text: String,
        category: String?,
        imageFile: File?
    ): Result<Report> {
        return try {
            val deviceId = prefManager.deviceId

            val latPart = lat.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val lngPart = lng.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val textPart = text.toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryPart = category?.toRequestBody("text/plain".toMediaTypeOrNull())

            val imagePart = imageFile?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", it.name, requestFile)
            }

            val response =
                api.createReport(deviceId, latPart, lngPart, textPart, categoryPart, imagePart)
            if (response.code == 201 && response.data != null) {
                Result.success(response.data.toDomain())
            } else if (response.code == 429) {
                Result.failure(Exception("429: ${response.message ?: "Try again in 10 minutes"}"))
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun flagReport(reportId: Int): Result<FlagReportResult> {
        return try {
            val deviceId = prefManager.deviceId
            val response = api.flagReport(deviceId, reportId)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFcmToken(token: String, deviceId: String?): Result<Unit> {
        return try {
            val response = api.updateFcmToken(FcmTokenDto(token = token, deviceId = deviceId))
            if (response.code == 200) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFcmToken(token: String): Result<Unit> {
        return try {
            val response = api.deleteFcmToken(token)
            if (response.code == 200) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEvacuationPoints(
        lat: Double,
        lng: Double,
    ): Result<List<Evacuation>> {
        return try {
            val response = api.getEvacuation(lat, lng, limit = 10) // Map uses nearest 10
            if (response.code == 200 && response.data != null) {
                Result.success(response.data.map { it.toDomain() })
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEvacuationById(id: Int): Evacuation? {
        return dao.getEvacuations().firstOrNull()?.find { it.id == id }?.toDomain()
    }

    override suspend fun getReportsMap(
        lat: Double,
        lng: Double,
        radius: Double
    ): Result<List<Report>> {
        return try {
            val response = api.getReports(lat, lng, radius = 50.0) // Map uses 50km radius
            if (response.code == 200 && response.data != null) {
                Result.success(response.data.map { it.toDomain() })
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRoute(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): Result<com.andikas.pantaubumi.domain.model.Route> {
        return try {
            val coords = "$originLng,$originLat;$destLng,$destLat"
            val response = osrmApi.getRoute(coords)

            if (response.isSuccessful && response.body() != null) {
                val osrmResponse = response.body()!!
                if (osrmResponse.code == "Ok" && osrmResponse.routes.isNotEmpty()) {
                    val osrmRoute = osrmResponse.routes.first()

                    val steps = osrmRoute.geometry.coordinates.map { coord ->
                        com.andikas.pantaubumi.domain.model.RouteStep(
                            instruction = "",
                            distanceMeters = 0,
                            location = listOf(coord[0], coord[1])
                        )
                    }

                    val domainRoute = com.andikas.pantaubumi.domain.model.Route(
                        distanceMeters = osrmRoute.distance.toInt(),
                        durationSeconds = osrmRoute.duration.toInt(),
                        geometry = "",
                        steps = steps
                    )

                    Result.success(domainRoute)
                } else {
                    Result.failure(Exception("OSRM Route Error: ${osrmResponse.code}"))
                }
            } else {
                Result.failure(Exception("OSRM API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
