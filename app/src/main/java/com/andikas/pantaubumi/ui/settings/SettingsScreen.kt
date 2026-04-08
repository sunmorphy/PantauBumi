package com.andikas.pantaubumi.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andikas.pantaubumi.BuildConfig
import com.andikas.pantaubumi.ui.components.PantauBumiSecondaryHeader
import com.andikas.pantaubumi.ui.theme.PantauBumiTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    SettingsScreenContent(
        state = state,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme,
        onNavigateBack = onNavigateBack,
        onToggleFlood = viewModel::toggleFlood,
        onToggleLandslide = viewModel::toggleLandslide,
        onToggleEarthquake = viewModel::toggleEarthquake,
        onSetMinRiskThreshold = viewModel::setMinRiskThreshold,
        onDownloadMap = { viewModel.startMapDownload(context) },
        onCancelDownload = { viewModel.cancelMapDownload(context) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    state: SettingsUiState,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit,
    onNavigateBack: () -> Unit,
    onToggleFlood: (Boolean) -> Unit,
    onToggleLandslide: (Boolean) -> Unit,
    onToggleEarthquake: (Boolean) -> Unit,
    onSetMinRiskThreshold: (Int) -> Unit,
    onDownloadMap: () -> Unit,
    onCancelDownload: () -> Unit
) {
    Scaffold(
        topBar = {
            PantauBumiSecondaryHeader(
                isDarkTheme = isDarkTheme,
                onNavigateBack = onNavigateBack,
                onToggleTheme = onToggleTheme
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Notifikasi Bencana",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Banjir")
                    Switch(
                        checked = state.notifyFlood,
                        onCheckedChange = onToggleFlood
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Longsor")
                    Switch(
                        checked = state.notifyLandslide,
                        onCheckedChange = onToggleLandslide
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Gempa Bumi")
                    Switch(
                        checked = state.notifyEarthquake,
                        onCheckedChange = onToggleEarthquake
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Ambang Risiko Minimum",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                val riskLabels = listOf("Rendah", "Sedang", "Tinggi")
                Text(
                    "Terima peringatan mulai tingkat: ${riskLabels[state.minRiskThreshold]}",
                    fontSize = 14.sp
                )
                Slider(
                    value = state.minRiskThreshold.toFloat(),
                    onValueChange = { onSetMinRiskThreshold(it.toInt()) },
                    valueRange = 0f..2f,
                    steps = 1
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Peta Offline (MapLibre)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Unduh peta area sekitar Anda agar tetap dapat diakses tanpa koneksi internet (${state.mapStorageSize} tersimpan).",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (state.mapDownloadProgress != null) {
                    Button(
                        onClick = onCancelDownload,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Batal Mengunduh")
                    }
                } else {
                    Button(
                        onClick = onDownloadMap,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unduh Peta Offline (Radius 30km)")
                    }
                }

                state.mapDownloadProgress?.let { progress ->
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = ProgressIndicatorDefaults.linearColor,
                        trackColor = ProgressIndicatorDefaults.linearTrackColor,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    )
                    Text(
                        "Mengunduh: $progress%",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Tentang",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PantauBumi v${BuildConfig.VERSION_NAME}", fontSize = 14.sp)
                }
                Text(
                    "Data bersumber dari BMKG, USGS, Open-Meteo, dan Laporan Komunitas (PetaBencana).",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode"
)
@Composable
fun SettingsScreenPreview() {
    PantauBumiTheme {
        SettingsScreenContent(
            state = SettingsUiState(
                notifyFlood = true,
                notifyLandslide = false,
                notifyEarthquake = true,
                minRiskThreshold = 1,
                mapDownloadProgress = 45,
                mapStorageSize = "124 MB"
            ),
            isDarkTheme = false,
            onToggleTheme = {},
            onNavigateBack = {},
            onToggleFlood = {},
            onToggleLandslide = {},
            onToggleEarthquake = {},
            onSetMinRiskThreshold = {},
            onDownloadMap = {},
            onCancelDownload = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun SettingsScreenDarkPreview() {
    PantauBumiTheme {
        SettingsScreenContent(
            state = SettingsUiState(
                notifyFlood = true,
                notifyLandslide = false,
                notifyEarthquake = true,
                minRiskThreshold = 1,
                mapDownloadProgress = 45,
                mapStorageSize = "124 MB"
            ),
            isDarkTheme = true,
            onToggleTheme = {},
            onNavigateBack = {},
            onToggleFlood = {},
            onToggleLandslide = {},
            onToggleEarthquake = {},
            onSetMinRiskThreshold = {},
            onDownloadMap = {},
            onCancelDownload = {}
        )
    }
}
