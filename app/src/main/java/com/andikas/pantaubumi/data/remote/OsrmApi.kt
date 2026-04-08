package com.andikas.pantaubumi.data.remote

import com.andikas.pantaubumi.data.remote.model.OsrmResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface OsrmApi {
    @GET("route/v1/driving/{coordinates}?overview=full&geometries=geojson")
    suspend fun getRoute(
        @Path("coordinates") coordinates: String
    ): Response<OsrmResponse>
}
