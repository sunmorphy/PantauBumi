package com.andikas.pantaubumi.ui.map

import android.Manifest
import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andikas.pantaubumi.R
import com.andikas.pantaubumi.domain.model.Evacuation
import com.andikas.pantaubumi.domain.model.Report
import com.andikas.pantaubumi.ui.components.PantauBumiBottomNavigation
import com.andikas.pantaubumi.ui.components.PantauBumiHeader
import com.andikas.pantaubumi.ui.theme.PantauBumiColors
import com.andikas.pantaubumi.ui.theme.PantauBumiTheme
import com.andikas.pantaubumi.util.bitmapDescriptorFromVector
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolygonOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    targetEvacuationId: Int = -1
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            viewModel.fetchLocation()
        }
    }

    LaunchedEffect(targetEvacuationId) {
        if (targetEvacuationId != -1) {
            viewModel.loadTargetEvacuation(targetEvacuationId)
        }
    }

    if (!locationPermissionsState.allPermissionsGranted) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text("Aplikasi memerlukan izin lokasi untuk menampilkan peta di sekitar Anda.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                    Text("Berikan Izin Lokasi")
                }
            }
        }
    } else {
        MapContent(
            state = state,
            onViewportChanged = viewModel::onViewportChanged,
            onEvacuationSelected = viewModel::onEvacuationSelected,
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            onNavigate = onNavigate
        )
    }
}

@Composable
fun MapContent(
    state: MapUiState,
    onViewportChanged: (Double, Double, Double) -> Unit,
    onEvacuationSelected: (Evacuation?) -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigate: (String) -> Unit
) {
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
                selectedRoute = "map",
                onRouteSelected = onNavigate
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            MapLibreView(
                state = state,
                onViewportChanged = onViewportChanged,
                onEvacuationSelected = onEvacuationSelected,
                modifier = Modifier.fillMaxSize()
            )

            MapOverlays(
                state = state,
                onEvacuationSelected = onEvacuationSelected
            )
        }
    }
}

@Composable
fun MapOverlays(
    state: MapUiState,
    onEvacuationSelected: (Evacuation?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth()) {
            MapLegend()
        }

        state.selectedEvacuation?.let { evacuation ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = evacuation.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { onEvacuationSelected(null) }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Text(
                        text = evacuation.address,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { evacuation.capacity / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape),
                        color =
                            if (evacuation.capacity > 100) PantauBumiColors.RiskLow
                            else if (evacuation.capacity > 50) PantauBumiColors.RiskMedium
                            else PantauBumiColors.RiskHigh
                    )
                    Text(
                        text = "Kapasitas: ${evacuation.capacity} Orang",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.End),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MapLegend() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(
                alpha = 0.9f
            )
        ),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Keterangan",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            LegendItem(Color(0xFF09849A), R.drawable.ic_map_user, "Lokasi Anda")
            LegendItem(Color(0xFF4EAE0A), R.drawable.ic_map_evacuation, "Titik Evakuasi")
            LegendItem(Color(0xFF557FF1), R.drawable.ic_flood, "Banjir")
            LegendItem(Color(0xFF8C545D), R.drawable.ic_landslide, "Longsor")
            LegendItem(Color(0xFFED2A1D), R.drawable.ic_volcano, "Gempa")
        }
    }
}

@Composable
fun LegendItem(color: Color, iconRes: Int, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = PantauBumiColors.Earth50,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun MapLibreView(
    state: MapUiState,
    onViewportChanged: (Double, Double, Double) -> Unit,
    onEvacuationSelected: (Evacuation?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier.background(PantauBumiColors.UnknownColor),
            contentAlignment = Alignment.Center
        ) {
            Text("Map View Placeholder")
        }
        return
    }

    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

    val userIcon = remember(context) {
        bitmapDescriptorFromVector(
            context,
            R.drawable.ic_map_user,
            PantauBumiColors.UserColor.toArgb()
        )
    }
    val floodIcon = remember(context) {
        bitmapDescriptorFromVector(
            context,
            R.drawable.ic_flood,
            PantauBumiColors.FloodColor.toArgb()
        )
    }
    val landslideIcon = remember(context) {
        bitmapDescriptorFromVector(
            context,
            R.drawable.ic_landslide,
            PantauBumiColors.LandslideColor.toArgb()
        )
    }
    val earthquakeIcon = remember(context) {
        bitmapDescriptorFromVector(
            context,
            R.drawable.ic_volcano,
            PantauBumiColors.EarthquakeColor.toArgb()
        )
    }
    val evacuationIcon = remember(context) {
        bitmapDescriptorFromVector(
            context,
            R.drawable.ic_map_evacuation,
            PantauBumiColors.EvacuationColor.toArgb()
        )
    }
    val defaultIcon = remember(context) {
        bitmapDescriptorFromVector(
            context,
            R.drawable.ic_map_report,
            PantauBumiColors.UnknownColor.toArgb()
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var initialCameraSet by remember { mutableStateOf(false) }

    LaunchedEffect(state.userLat, state.userLng, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val lat = state.userLat ?: return@LaunchedEffect
        val lng = state.userLng ?: return@LaunchedEffect

        if (!initialCameraSet) {
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(lat, lng))
                .zoom(14.0)
                .build()
            initialCameraSet = true
        }
    }

    LaunchedEffect(
        state.riskZones,
        state.evacuationPoints,
        state.reports,
        state.route,
        state.userLat,
        state.userLng
    ) {
        val map = mapLibreMap ?: return@LaunchedEffect
        map.clear()

        // Draw Risk Zones (Polygons)
        state.riskZones?.features?.forEach { feature ->
            val points = feature.geometry.coordinates.firstOrNull()?.map { LatLng(it[1], it[0]) }
            if (!points.isNullOrEmpty()) {
                val color = when (feature.riskLevel.lowercase()) {
                    "high" -> PantauBumiColors.RiskHigh
                    "medium" -> PantauBumiColors.RiskMedium
                    else -> PantauBumiColors.RiskLow
                }.toArgb()

                map.addPolygon(
                    PolygonOptions()
                        .addAll(points)
                        .fillColor(color)
                        .alpha(0.4f)
                )
            }
        }

        // Draw Route (Polyline)
        state.route?.let { route ->
            val routePoints = route.steps.map { LatLng(it.location[1], it.location[0]) }
            if (routePoints.isNotEmpty()) {
                map.addPolyline(
                    PolylineOptions()
                        .addAll(routePoints)
                        .color(PantauBumiColors.RouteColor.toArgb())
                        .width(5f)
                )
            }
        }

        // Add user location marker
        if (state.userLat != null && state.userLng != null) {
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(state.userLat, state.userLng))
                    .icon(userIcon)
                    .title("Lokasi Anda")
            )
        }

        // Add evacuation markers
        state.evacuationPoints.forEach { evacuation ->
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(evacuation.lat, evacuation.lng))
                    .title(evacuation.name)
                    .icon(evacuationIcon)
                    .snippet("Kapasitas: ${evacuation.capacity}%")
            )
        }

        // Add report markers
        state.reports.forEach { report ->
            val icon = when {
                report.category.contains("banjir", ignoreCase = true) -> floodIcon
                report.category.contains("longsor", ignoreCase = true) -> landslideIcon
                report.category.contains("gempa", ignoreCase = true) -> earthquakeIcon
                else -> defaultIcon
            }

            map.addMarker(
                MarkerOptions()
                    .position(LatLng(report.lat, report.lng))
                    .title(report.category)
                    .icon(icon)
                    .snippet(report.text)
            )
        }
    }

    LaunchedEffect(state.route, state.selectedEvacuation) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val route = state.route
        val selected = state.selectedEvacuation
        val userLat = state.userLat
        val userLng = state.userLng

        if (route != null && selected != null && userLat != null && userLng != null) {
            val bounds = LatLngBounds.Builder()
                .include(LatLng(userLat, userLng))
                .include(LatLng(selected.lat, selected.lng))
                .build()

            map.animateCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, 200),
                1000
            )
        } else if (selected != null) {
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(selected.lat, selected.lng), 14.0),
                1000
            )
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            if (mapLibreMap == null) {
                view.getMapAsync { map ->
                    mapLibreMap = map
                    map.setStyle("https://tiles.openfreemap.org/styles/liberty")
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(-6.2297, 106.8295))
                        .zoom(12.0)
                        .build()

                    map.addOnCameraIdleListener {
                        map.cameraPosition.target?.let { target ->
                            onViewportChanged(target.latitude, target.longitude, 5.0)
                        }
                    }
                }
            }

            mapLibreMap?.setOnMarkerClickListener { marker ->
                val evacuation = state.evacuationPoints.find {
                    it.name == marker.title
                }
                if (evacuation != null) {
                    onEvacuationSelected(evacuation)
                }
                true
            }
        }
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode"
)
@Composable
fun MapScreenPreview() {
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

    val sampleState = MapUiState(
        evacuationPoints = listOf(sampleEvacuation),
        selectedEvacuation = sampleEvacuation,
        locationName = "Jakarta Selatan",
        reports = listOf(
            Report(
                id = 1,
                lat = -6.23,
                lng = 106.83,
                text = "Banjir setinggi 50cm",
                category = "Banjir",
                verified = true,
                verificationScore = 0.85,
                source = "Laporan Warga",
                flagCount = 0,
                createdAt = "2024-03-20T10:00:00Z"
            )
        )
    )

    PantauBumiTheme {
        MapContent(
            state = sampleState,
            isDarkTheme = false,
            onViewportChanged = { _, _, _ -> },
            onEvacuationSelected = { _ -> },
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
fun MapScreenDarkPreview() {
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

    val sampleState = MapUiState(
        evacuationPoints = listOf(sampleEvacuation),
        selectedEvacuation = sampleEvacuation,
        locationName = "Jakarta Selatan",
        reports = listOf(
            Report(
                id = 1,
                lat = -6.23,
                lng = 106.83,
                text = "Banjir setinggi 50cm",
                category = "Banjir",
                verified = true,
                verificationScore = 0.85,
                source = "Laporan Warga",
                flagCount = 0,
                createdAt = "2024-03-20T10:00:00Z"
            )
        )
    )

    PantauBumiTheme {
        MapContent(
            state = sampleState,
            isDarkTheme = true,
            onViewportChanged = { _, _, _ -> },
            onEvacuationSelected = { _ -> },
            onNavigate = {}
        )
    }
}
