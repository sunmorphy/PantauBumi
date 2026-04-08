package com.andikas.pantaubumi.data.remote

import com.andikas.pantaubumi.data.remote.model.AlertsResponseDto
import com.andikas.pantaubumi.data.remote.model.ApiResponse
import com.andikas.pantaubumi.data.remote.model.EvacuationDto
import com.andikas.pantaubumi.data.remote.model.FcmTokenDto
import com.andikas.pantaubumi.data.remote.model.FlagReportResponse
import com.andikas.pantaubumi.data.remote.model.ReportDto
import com.andikas.pantaubumi.data.remote.model.RiskDto
import com.andikas.pantaubumi.data.remote.model.WeatherDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PantauBumiApi {

    @GET("health")
    suspend fun checkHealth(): Response<Unit>

    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): ApiResponse<WeatherDto>

    @GET("risk")
    suspend fun getRisk(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): ApiResponse<RiskDto>

    @GET("alerts")
    suspend fun getAlerts(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius_km") radiusKm: Double? = null,
        @Query("hours") hours: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("before_id") beforeId: Int? = null
    ): ApiResponse<AlertsResponseDto>

    @GET("evacuation")
    suspend fun getEvacuation(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("limit") limit: Int? = null
    ): ApiResponse<List<EvacuationDto>>

    @GET("reports")
    suspend fun getReports(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Double? = null,
        @Query("category") category: String? = null,
        @Query("limit") limit: Int? = null
    ): ApiResponse<List<ReportDto>>

    @Multipart
    @POST("reports")
    suspend fun createReport(
        @Header("X-Device-ID") deviceId: String,
        @Part("lat") lat: RequestBody,
        @Part("lng") lng: RequestBody,
        @Part("text") text: RequestBody,
        @Part("category") category: RequestBody?,
        @Part image: MultipartBody.Part? = null
    ): ApiResponse<ReportDto>

    @POST("reports/{id}/flag")
    suspend fun flagReport(
        @Header("X-Device-ID") deviceId: String,
        @Path("id") id: Int
    ): ApiResponse<FlagReportResponse>

    @POST("fcm-token")
    suspend fun updateFcmToken(
        @Body request: FcmTokenDto
    ): ApiResponse<FcmTokenDto>

    @DELETE("fcm-token")
    suspend fun deleteFcmToken(
        @Query("token") token: String
    ): ApiResponse<Unit>
}
