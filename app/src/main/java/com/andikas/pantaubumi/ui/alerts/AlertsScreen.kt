package com.andikas.pantaubumi.ui.alerts

import android.Manifest
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.andikas.pantaubumi.R
import com.andikas.pantaubumi.domain.model.Alert
import com.andikas.pantaubumi.ui.components.EmptyState
import com.andikas.pantaubumi.ui.components.PantauBumiBottomNavigation
import com.andikas.pantaubumi.ui.components.PantauBumiHeader
import com.andikas.pantaubumi.ui.theme.PantauBumiColors
import com.andikas.pantaubumi.ui.theme.PantauBumiTheme
import com.andikas.pantaubumi.util.formatTime
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.flow.flowOf
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = hiltViewModel(),
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            viewModel.fetchLocationAndLoadAlerts()
        }
    }

    if (!locationPermissionsState.allPermissionsGranted) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text("Izin lokasi diperlukan untuk menampilkan peringatan di sekitar Anda.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                    Text("Berikan Izin")
                }
            }
        }
    } else {
        AlertsScreen(
            state = state,
            onRefresh = { viewModel.fetchLocationAndLoadAlerts(isRefresh = true) },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            onNavigate = onNavigate
        )
    }
}

@Composable
internal fun AlertsScreen(
    state: AlertsUiState,
    onRefresh: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigate: (String) -> Unit
) {
    val alerts = state.alerts.collectAsLazyPagingItems()

    AlertsContent(
        state = state,
        alerts = alerts,
        onRefresh = onRefresh,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme,
        onNavigate = onNavigate
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AlertsContent(
    state: AlertsUiState,
    alerts: androidx.paging.compose.LazyPagingItems<Alert>,
    onRefresh: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigate: (String) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = onRefresh
    )

    val currentDate = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val uiDateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

    val groupedAlerts = remember(alerts.itemSnapshotList.items) {
        alerts.itemSnapshotList.items.groupBy { alert ->
            val alertDate = LocalDate.parse(alert.createdAt.substring(0, 10))
            when {
                alertDate.isEqual(currentDate) -> "HARI INI"
                alertDate.isEqual(currentDate.minusDays(1)) -> "KEMARIN"
                else -> alertDate.format(dateFormatter).uppercase()
            }
        }.toSortedMap { key1, key2 ->
            val rank = { key: String ->
                when (key) {
                    "HARI INI" -> 0
                    "KEMARIN" -> 1
                    else -> 2
                }
            }

            val r1 = rank(key1)
            val r2 = rank(key2)

            if (r1 != r2) {
                r1.compareTo(r2)
            } else {
                val d1 = if (r1 == 2) LocalDate.parse(key1, dateFormatter) else currentDate
                val d2 = if (r2 == 2) LocalDate.parse(key2, dateFormatter) else currentDate
                d2.compareTo(d1)
            }
        }
    }

    Scaffold(
        topBar = {
            PantauBumiHeader(
                locationName = state.locationName,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onSettingsClick = { onNavigate("settings") }
            )
        },
        bottomBar = {
            PantauBumiBottomNavigation(
                selectedRoute = "alerts",
                onRouteSelected = onNavigate
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp)
            ) {
                if (state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PantauBumiColors.Green600)
                        }
                    }
                } else {
                    if (alerts.itemCount == 0) {
                        item {
                            EmptyState(
                                modifier = Modifier.fillParentMaxSize(),
                                icon = Icons.Default.NotificationsOff,
                                title = "Tidak Ada Peringatan",
                                description = "Saat ini tidak ada peringatan bencana di sekitar lokasi Anda. Tetap waspada!"
                            )
                        }
                    } else {
                        groupedAlerts.forEach { (dateHeader, itemsInGroup) ->
                            stickyHeader(key = dateHeader) {
                                val displayHeader = when (dateHeader) {
                                    "HARI INI", "KEMARIN" -> dateHeader
                                    else -> LocalDate.parse(dateHeader, dateFormatter)
                                        .format(uiDateFormatter).uppercase()
                                }
                                Text(
                                    text = displayHeader,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(vertical = 12.dp)
                                )
                            }

                            items(
                                count = itemsInGroup.size,
                                key = { index -> itemsInGroup[index].id }
                            ) { index ->
                                val alert = itemsInGroup[index]
                                AlertItem(alert)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        if (alerts.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = PantauBumiColors.Green600,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = state.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = PantauBumiColors.Green600
            )
        }
    }
}

@Composable
fun AlertItem(alert: Alert) {
    val icon = when (alert.type.lowercase()) {
        "banjir" -> ImageVector.vectorResource(id = R.drawable.ic_flood)
        "longsor" -> ImageVector.vectorResource(id = R.drawable.ic_landslide)
        "gempa", "gempa bumi" -> ImageVector.vectorResource(id = R.drawable.ic_volcano)
        else -> ImageVector.vectorResource(id = R.drawable.ic_globe_question)
    }
    val severityColor = when (alert.severity.lowercase()) {
        "high", "critical", "kritis", "tinggi" -> PantauBumiColors.RiskHigh
        "medium", "sedang" -> PantauBumiColors.RiskMedium
        else -> PantauBumiColors.RiskLow
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = severityColor,
                modifier = Modifier
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.type,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = alert.message,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatTime(alert.createdAt),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.Top)
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode"
)
@Composable
fun AlertsScreenPreview() {
    val sampleAlerts = listOf(
        Alert(
            id = 1,
            type = "Banjir",
            lat = -6.2,
            lng = 106.8,
            severity = "High",
            message = "Waspada banjir di wilayah Jakarta Selatan",
            source = "BPBD",
            createdAt = "2023-10-27T10:30:00Z"
        ),
        Alert(
            id = 2,
            type = "Gempa Bumi",
            lat = -6.3,
            lng = 106.9,
            severity = "Medium",
            message = "Getaran dirasakan di Depok",
            source = "BMKG",
            createdAt = "2023-10-27T09:15:00Z"
        ),
        Alert(
            id = 3,
            type = "Cuaca Ekstrem",
            lat = -6.1,
            lng = 106.7,
            severity = "Low",
            message = "Hujan ringan hingga sedang",
            source = "BMKG",
            createdAt = "2023-10-26T08:00:00Z"
        ),
        Alert(
            id = 4,
            type = "Cuaca Super Ekstrem",
            lat = -6.1,
            lng = 106.7,
            severity = "Low",
            message = "Hujan ringan hingga sedang",
            source = "BMKG",
            createdAt = "2023-10-26T08:00:00Z"
        ),
        Alert(
            id = 5,
            type = "Cuaca Biasa",
            lat = -6.1,
            lng = 106.7,
            severity = "Low",
            message = "Hujan ringan hingga sedang",
            source = "BMKG",
            createdAt = "2026-03-18T08:00:00Z"
        )
    )

    val state = AlertsUiState(
        alerts = flowOf(PagingData.from(sampleAlerts)),
        locationName = "Jakarta Selatan"
    )

    PantauBumiTheme {
        AlertsScreen(
            state = state,
            isDarkTheme = false,
            onRefresh = {},
            onNavigate = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun AlertsScreenDarkPreview() {
    val sampleAlerts = listOf(
        Alert(
            id = 1,
            type = "Banjir",
            lat = -6.2,
            lng = 106.8,
            severity = "High",
            message = "Waspada banjir di wilayah Jakarta Selatan",
            source = "BPBD",
            createdAt = "2023-10-27T10:30:00Z"
        ),
        Alert(
            id = 2,
            type = "Gempa Bumi",
            lat = -6.3,
            lng = 106.9,
            severity = "Medium",
            message = "Getaran dirasakan di Depok",
            source = "BMKG",
            createdAt = "2023-10-27T09:15:00Z"
        ),
        Alert(
            id = 3,
            type = "Cuaca Ekstrem",
            lat = -6.1,
            lng = 106.7,
            severity = "Low",
            message = "Hujan ringan hingga sedang",
            source = "BMKG",
            createdAt = "2023-10-26T08:00:00Z"
        ),
        Alert(
            id = 4,
            type = "Cuaca Super Ekstrem",
            lat = -6.1,
            lng = 106.7,
            severity = "Low",
            message = "Hujan ringan hingga sedang",
            source = "BMKG",
            createdAt = "2023-10-26T08:00:00Z"
        ),
        Alert(
            id = 5,
            type = "Cuaca Biasa",
            lat = -6.1,
            lng = 106.7,
            severity = "Low",
            message = "Hujan ringan hingga sedang",
            source = "BMKG",
            createdAt = "2026-03-18T08:00:00Z"
        )
    )

    val state = AlertsUiState(
        alerts = flowOf(PagingData.from(sampleAlerts)),
        locationName = "Jakarta Selatan"
    )

    PantauBumiTheme {
        AlertsScreen(
            state = state,
            isDarkTheme = true,
            onRefresh = {},
            onNavigate = {}
        )
    }
}
