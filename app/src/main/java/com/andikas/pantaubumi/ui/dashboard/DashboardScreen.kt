package com.andikas.pantaubumi.ui.dashboard

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.Water
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andikas.pantaubumi.R
import com.andikas.pantaubumi.domain.model.Evacuation
import com.andikas.pantaubumi.domain.model.Risk
import com.andikas.pantaubumi.domain.model.Weather
import com.andikas.pantaubumi.ui.components.PantauBumiBottomNavigation
import com.andikas.pantaubumi.ui.components.PantauBumiHeader
import com.andikas.pantaubumi.ui.theme.PantauBumiColors
import com.andikas.pantaubumi.ui.theme.PantauBumiTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
            add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val permissionState = rememberMultiplePermissionsState(permissions)

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            viewModel.fetchLocationAndLoadData()
        }
    }

    if (!permissionState.allPermissionsGranted) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "PantauBumi membutuhkan izin untuk memberikan informasi bencana di sekitar Anda.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                    Text("Berikan Izin")
                }
            }
        }
    } else {
        DashboardContent(
            state = state,
            onRefresh = { viewModel.fetchLocationAndLoadData(isRefresh = true) },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            onNavigate = onNavigate
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardContent(
    state: DashboardUiState,
    onRefresh: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = onRefresh
    )

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
                selectedRoute = "dashboard",
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
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PantauBumiColors.Green600)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                ) {
                    state.risk?.let { risk ->
                        RiskBanner(risk)
                        Spacer(modifier = Modifier.height(24.dp))
                        StatusBahayaSection(isDarkTheme, risk)
                        Spacer(modifier = Modifier.height(24.dp))
                        DetailedStatsSection(isDarkTheme, state.weather)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    EvacuationSection(state.evacuations.firstOrNull(), onNavigate)
                    Spacer(modifier = Modifier.height(80.dp))
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
private fun RiskBanner(risk: Risk) {
    val isHigh = risk.overallRisk.lowercase() in listOf("high", "critical")
    val isMid = risk.overallRisk.lowercase() in listOf("medium")
    val bgColor =
        if (isHigh) PantauBumiColors.RiskHigh
        else if (isMid) PantauBumiColors.RiskMedium
        else PantauBumiColors.RiskLow

    val timeAgo = remember(risk.computedAt) {
        try {
            val diffMillis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val instant = Instant.parse(risk.computedAt)
                val now = Instant.now()
                Duration.between(instant, now).toMillis()
            } else {
                val sdf =
                    java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val date = sdf.parse(risk.computedAt)
                val now = System.currentTimeMillis()
                now - date!!.time
            }
            val minutes = diffMillis / 60000
            when {
                minutes < 1 -> "baru saja"
                minutes < 60 -> "$minutes menit lalu"
                minutes < 1440 -> "${minutes / 60} jam lalu"
                minutes < 10080 -> "${minutes / 1440} hari lalu"
                minutes < 43200 -> "${minutes / 10080} minggu lalu"
                minutes < 525600 -> "${minutes / 43200} bulan lalu"
                else -> "${minutes / 525600} tahun lalu"
            }
        } catch (_: Exception) {
            "2 menit lalu"
        }
    }

    fun buildPredictionText(floodScore: Double, landslideScore: Double): String {
        return when {
            floodScore >= 0.75 -> "Prediksi AI: risiko banjir kritis dalam 2 jam"
            floodScore >= 0.50 -> "Prediksi AI: potensi banjir meningkat"
            landslideScore >= 0.50 -> "Prediksi AI: tanah jenuh, waspadai longsor"
            else -> "Prediksi AI: kondisi relatif aman"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Warning,
                contentDescription = null,
                tint = if (isHigh) PantauBumiColors.RiskHighBg else PantauBumiColors.RiskLowBg,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isHigh) "Tinggi — Waspada Banjir" else "Rendah — Aman",
                color = if (isHigh) PantauBumiColors.RiskHighBg else PantauBumiColors.RiskLowBg,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isHigh) PantauBumiColors.RiskHighText.copy(alpha = 0.2f)
                        else PantauBumiColors.RiskLowText.copy(alpha = 0.2f)
                    )
                    .padding(vertical = 2.dp, horizontal = 8.dp)
            ) {
                Text(
                    "Diperbarui $timeAgo",
                    color = if (isHigh) PantauBumiColors.RiskHighBg else PantauBumiColors.RiskLowBg,
                    fontSize = 10.sp
                )
            }
        }
        Text(
            text = buildPredictionText(risk.floodScore, risk.landslideScore),
            color = if (isHigh) PantauBumiColors.RiskHighBg else PantauBumiColors.RiskLowBg,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun StatusBahayaSection(isDarkTheme: Boolean, risk: Risk) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            "STATUS BAHAYA",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(
                isDarkTheme,
                Modifier.weight(1f),
                "Banjir",
                risk.floodScore,
                ImageVector.vectorResource(id = R.drawable.ic_flood)
            )
            StatusCard(
                isDarkTheme,
                Modifier.weight(1f),
                "Longsor",
                risk.landslideScore,
                ImageVector.vectorResource(id = R.drawable.ic_landslide)
            )
            StatusCard(
                isDarkTheme,
                Modifier.weight(1f),
                "Gempa",
                risk.earthquakeScore,
                ImageVector.vectorResource(id = R.drawable.ic_volcano)
            )
        }
    }
}

@Composable
private fun StatusCard(
    isDarkTheme: Boolean,
    modifier: Modifier,
    title: String,
    score: Double,
    icon: ImageVector
) {
    val level = when {
        score >= 0.75 -> "KRITIS"
        score >= 0.5 -> "TINGGI"
        score >= 0.25 -> "SEDANG"
        else -> "RENDAH"
    }
    val color = when (level) {
        "KRITIS", "TINGGI" -> PantauBumiColors.RiskHigh
        "SEDANG" -> PantauBumiColors.RiskMedium
        else -> PantauBumiColors.RiskLow
    }
    val bgColor = when (level) {
        "KRITIS", "TINGGI" -> if (isDarkTheme) PantauBumiColors.RiskHighBg else PantauBumiColors.RiskHigh
        "SEDANG" -> if (isDarkTheme) PantauBumiColors.RiskMediumBg else PantauBumiColors.RiskMedium
        else -> if (isDarkTheme) PantauBumiColors.RiskLowBg else PantauBumiColors.RiskLow
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(bgColor.copy(alpha = if (isDarkTheme) 0.9f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = bgColor.copy(alpha = if (isDarkTheme) 0.9f else 0.1f),
                shape = RoundedCornerShape(64.dp)
            ) {
                Text(
                    text = level,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DetailedStatsSection(isDarkTheme: Boolean, weather: Weather?) {
    if (weather == null) return

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Curah Hujan
        val rainStatus = when {
            weather.rainfallMmPerHour >= 50 -> "Ekstrem"
            weather.rainfallMmPerHour >= 20 -> "Sangat Lebat"
            weather.rainfallMmPerHour >= 10 -> "Lebat"
            weather.rainfallMmPerHour >= 5 -> "Sedang"
            else -> "Ringan"
        }
        val rainColor = when {
            weather.rainfallMmPerHour >= 20 -> PantauBumiColors.RiskHigh
            weather.rainfallMmPerHour >= 10 -> PantauBumiColors.RiskMedium
            else -> PantauBumiColors.RiskLow
        }
        val rainBgColor = when {
            weather.rainfallMmPerHour >= 10 -> if (isDarkTheme) PantauBumiColors.RiskHighBg else PantauBumiColors.RiskHigh
            weather.rainfallMmPerHour >= 5 -> if (isDarkTheme) PantauBumiColors.RiskMediumBg else PantauBumiColors.RiskMedium
            else -> if (isDarkTheme) PantauBumiColors.RiskLowBg else PantauBumiColors.RiskLow
        }

        DetailCard(
            isDarkTheme = isDarkTheme,
            icon = ImageVector.vectorResource(id = R.drawable.ic_rainy),
            title = "CURAH HUJAN",
            value = "${weather.rainfallMmPerHour}mm/jam",
            status = rainStatus,
            statusColor = rainColor,
            statusBgColor = rainBgColor
        )

        // Level Sungai
        val riverStatus = when {
            weather.riverLevelM >= 3.0 -> "Siaga 1"
            weather.riverLevelM >= 2.0 -> "Siaga 2"
            weather.riverLevelM >= 1.0 -> "Siaga 3"
            else -> "Normal"
        }
        val riverColor = when {
            weather.riverLevelM >= 2.0 -> PantauBumiColors.RiskHigh
            weather.riverLevelM >= 1.0 -> PantauBumiColors.RiskMedium
            else -> PantauBumiColors.RiskLow
        }
        val riverBgColor = when {
            weather.riverLevelM >= 2.0 -> if (isDarkTheme) PantauBumiColors.RiskHighBg else PantauBumiColors.RiskHigh
            weather.riverLevelM >= 1.0 -> if (isDarkTheme) PantauBumiColors.RiskMediumBg else PantauBumiColors.RiskMedium
            else -> if (isDarkTheme) PantauBumiColors.RiskLowBg else PantauBumiColors.RiskLow
        }

        DetailCard(
            isDarkTheme = isDarkTheme,
            icon = Icons.Outlined.Water,
            title = "LEVEL SUNGAI",
            value = "${weather.riverLevelM}m",
            status = riverStatus,
            statusColor = riverColor,
            statusBgColor = riverBgColor
        )

        // Magnitudo
        val magStatus = weather.latestMagnitude?.let {
            when {
                it >= 7.0 -> "Mayor"
                it >= 5.0 -> "Kuat"
                it >= 3.0 -> "Sedang"
                else -> "Ringan"
            }
        } ?: "N/A"
        val magColor = weather.latestMagnitude?.let {
            when {
                it >= 5.0 -> PantauBumiColors.RiskHigh
                it >= 3.0 -> PantauBumiColors.RiskMedium
                else -> PantauBumiColors.RiskLow
            }
        } ?: MaterialTheme.colorScheme.outlineVariant
        val magBgColor = weather.latestMagnitude?.let {
            when {
                it >= 5.0 -> if (isDarkTheme) PantauBumiColors.RiskHighBg else PantauBumiColors.RiskHigh
                it >= 3.0 -> if (isDarkTheme) PantauBumiColors.RiskMediumBg else PantauBumiColors.RiskMedium
                else -> if (isDarkTheme) PantauBumiColors.RiskLowBg else PantauBumiColors.RiskLow
            }
        } ?: MaterialTheme.colorScheme.outline

        DetailCard(
            isDarkTheme = isDarkTheme,
            icon = ImageVector.vectorResource(id = R.drawable.ic_earthquake),
            title = "MAGNITUDO",
            value = weather.latestMagnitude?.toString() ?: "—",
            status = magStatus,
            statusColor = magColor,
            statusBgColor = magBgColor
        )
    }
}

@Composable
private fun DetailCard(
    isDarkTheme: Boolean,
    icon: ImageVector,
    title: String,
    value: String,
    status: String,
    statusColor: Color,
    statusBgColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusBgColor.copy(alpha = if (isDarkTheme) 0.9f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = statusColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Surface(
                color = statusBgColor.copy(alpha = if (isDarkTheme) 0.9f else 0.1f),
                shape = RoundedCornerShape(64.dp)
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EvacuationSection(evacuation: Evacuation?, onNavigate: (String) -> Unit = {}) {
    if (evacuation == null) return

    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Titik Evakuasi Terdekat",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Surface(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Text(
                        "Aktif",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                evacuation.name,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${evacuation.distanceKm} km",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Kapasitas ${evacuation.capacity} Orang",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { onNavigate("map?evacuationId=${evacuation.id}") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lihat Rute", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode",
    heightDp = 1080
)
@Composable
fun DashboardPreview() {
    val sampleRisk = Risk(
        lat = -6.2,
        lng = 106.8,
        floodScore = 0.8,
        landslideScore = 0.2,
        earthquakeScore = 0.1,
        overallRisk = "High",
        computedAt = "2023-10-27T10:00:00Z"
    )

    val sampleWeather = Weather(
        rainfallMmPerHour = 15.5,
        riverLevelM = 2.4,
        riverLevelDeltaPerHour = 0.1,
        latestMagnitude = 4.2,
        recordedAt = "2023-10-27T10:00:00Z"
    )

    val sampleEvacuation = Evacuation(
        id = 1,
        name = "GOR Bulungan",
        lat = -6.244,
        lng = 106.799,
        capacity = 80,
        type = "Shelter",
        address = "Jl. Bulungan No.1",
        distanceKm = 1.2
    )

    val sampleDashboardState = DashboardUiState(
        risk = sampleRisk,
        weather = sampleWeather,
        evacuations = listOf(sampleEvacuation)
    )

    PantauBumiTheme {
        Row {
            DashboardContent(
                state = sampleDashboardState,
                onRefresh = {},
                isDarkTheme = false
            )
            DashboardContent(
                state = sampleDashboardState,
                onRefresh = {},
                isDarkTheme = true
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    heightDp = 1080
)
@Composable
fun DashboardDarkPreview() {
    val sampleRisk = Risk(
        lat = -6.2,
        lng = 106.8,
        floodScore = 0.8,
        landslideScore = 0.2,
        earthquakeScore = 0.1,
        overallRisk = "High",
        computedAt = "2023-10-27T10:00:00Z"
    )

    val sampleWeather = Weather(
        rainfallMmPerHour = 15.5,
        riverLevelM = 2.4,
        riverLevelDeltaPerHour = 0.1,
        latestMagnitude = 4.2,
        recordedAt = "2023-10-27T10:00:00Z"
    )

    val sampleEvacuation = Evacuation(
        id = 1,
        name = "GOR Bulungan",
        lat = -6.244,
        lng = 106.799,
        capacity = 80,
        type = "Shelter",
        address = "Jl. Bulungan No.1",
        distanceKm = 1.2
    )

    val sampleDashboardState = DashboardUiState(
        risk = sampleRisk,
        weather = sampleWeather,
        evacuations = listOf(sampleEvacuation)
    )

    PantauBumiTheme {
        DashboardContent(
            state = sampleDashboardState,
            onRefresh = {},
            isDarkTheme = true
        )
    }
}
