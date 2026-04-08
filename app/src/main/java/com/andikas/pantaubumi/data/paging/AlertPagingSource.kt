package com.andikas.pantaubumi.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.andikas.pantaubumi.data.mapper.toDomain
import com.andikas.pantaubumi.data.remote.PantauBumiApi
import com.andikas.pantaubumi.domain.model.Alert

class AlertPagingSource(
    private val api: PantauBumiApi,
    private val lat: Double,
    private val lng: Double
) : PagingSource<Int, Alert>() {

    override fun getRefreshKey(state: PagingState<Int, Alert>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Alert> {
        val cursor = params.key
        val limit = params.loadSize

        return try {
            val response = api.getAlerts(
                lat = lat,
                lng = lng,
                beforeId = cursor,
                limit = limit
            )

            if (response.code == 200 && response.data != null) {
                val alerts = response.data.items.map { it.toDomain() }
                LoadResult.Page(
                    data = alerts,
                    prevKey = null,
                    nextKey = if (response.data.hasMore) response.data.nextCursor else null
                )
            } else {
                LoadResult.Error(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
