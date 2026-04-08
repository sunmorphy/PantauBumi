package com.andikas.pantaubumi.ui.reports

import android.Manifest
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SpeakerNotesOff
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.andikas.pantaubumi.R
import com.andikas.pantaubumi.domain.model.Report
import com.andikas.pantaubumi.ui.components.EmptyState
import com.andikas.pantaubumi.ui.components.PantauBumiBottomNavigation
import com.andikas.pantaubumi.ui.components.PantauBumiChip
import com.andikas.pantaubumi.ui.components.PantauBumiHeader
import com.andikas.pantaubumi.ui.theme.PantauBumiColors
import com.andikas.pantaubumi.ui.theme.PantauBumiTheme
import com.andikas.pantaubumi.vo.HazardType
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel(),
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
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
            viewModel.fetchLocationAndLoadReports()
        }
    }

    if (!locationPermissionsState.allPermissionsGranted) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Aplikasi memerlukan izin lokasi untuk menampilkan laporan di sekitar Anda.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                    Text("Berikan Izin")
                }
            }
        }
    } else {
        ReportContent(
            state = state,
            onFilterSelected = viewModel::onFilterSelected,
            onFlagReport = viewModel::flagReport,
            onAddReportClick = { viewModel.onShowAddReportDialog(true) },
            onDismissDialog = { viewModel.onShowAddReportDialog(false) },
            onSubmitReport = viewModel::submitReport,
            onRefresh = { viewModel.fetchLocationAndLoadReports(isRefresh = true) },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            onNavigate = onNavigate
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ReportContent(
    state: ReportUiState,
    onFilterSelected: (HazardType) -> Unit,
    onFlagReport: (Int) -> Unit,
    onAddReportClick: () -> Unit,
    onDismissDialog: () -> Unit,
    onSubmitReport: (String, String, Uri?) -> Unit,
    onRefresh: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigate: (String) -> Unit
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
                selectedRoute = "report",
                onRouteSelected = onNavigate
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddReportClick,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Laporan")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullRefreshState)
        ) {
            Column {
                ReportFilterRow(
                    selectedFilter = state.selectedFilter,
                    onFilterSelected = onFilterSelected
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        if (state.reports.isEmpty()) {
                            item {
                                EmptyState(
                                    modifier = Modifier.fillParentMaxSize(),
                                    icon = Icons.Default.SpeakerNotesOff,
                                    title = "Belum Ada Laporan",
                                    description = "Saat ini belum ada laporan dari warga di sekitar Anda. Anda bisa menjadi yang pertama melaporkan kejadian!"
                                )
                            }
                        } else {
                            items(state.reports) { report ->
                                ReportCard(
                                    report = report,
                                    onFlag = { onFlagReport(report.id) }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
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

            if (state.showAddReportDialog) {
                AddReportDialog(
                    isSubmitting = state.isSubmitting,
                    cooldownMinutes = state.cooldownMinutes,
                    onDismiss = onDismissDialog,
                    onSubmit = onSubmitReport
                )
            }
        }
    }
}

@Composable
fun AddReportDialog(
    isSubmitting: Boolean,
    cooldownMinutes: Int,
    onDismiss: () -> Unit,
    onSubmit: (String, String, Uri?) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedHazardType by remember { mutableStateOf(HazardType.EARTHQUAKE) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Buat Laporan Baru",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Kategori Bencana",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HazardType.nonFilter().forEach { hazardType ->
                        val isSelected = selectedHazardType == hazardType
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    enabled = !isSubmitting,
                                    onClick = { selectedHazardType = hazardType }
                                ),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PantauBumiColors.Green600 else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Text(
                                text = hazardType.label,
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                color = if (isSelected) PantauBumiColors.Green100 else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    textStyle = MaterialTheme.typography.titleSmall,
                    placeholder = { Text("Ceritakan apa yang terjadi di lokasi Anda...") },
                    supportingText = { Text("Minimal 10 karakter") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PantauBumiColors.Green600,
                        cursorColor = PantauBumiColors.Green600,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                if (selectedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clickable { selectedImageUri = null },
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.5f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus Gambar",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambah Foto (Opsional)")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Batal")
                    }
                    val isCooldown = cooldownMinutes > 0
                    Button(
                        onClick = { onSubmit(text, selectedHazardType.label, selectedImageUri) },
                        modifier = Modifier.weight(1f),
                        enabled = text.isNotBlank() && !isSubmitting && !isCooldown,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PantauBumiColors.Green100,
                                strokeWidth = 2.dp
                            )
                        } else if (isCooldown) {
                            Text(
                                "Kirim Laporan ($cooldownMinutes menit lagi)",
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                        } else {
                            Text("Kirim")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportFilterRow(
    selectedFilter: HazardType,
    onFilterSelected: (HazardType) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HazardType.entries.forEach { type ->
            item {
                PantauBumiChip(
                    label = type.label,
                    isSelected = selectedFilter == type,
                    onClick = { onFilterSelected(type) }
                )
            }
        }
    }
}

@Composable
fun ReportCard(
    report: Report,
    onFlag: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val currentDate = LocalDate.now()
    val uiDateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id"))

    val date = LocalDate.parse(report.createdAt.substring(0, 10))
    val reportDate = when {
        date.isEqual(currentDate) -> "HARI INI"
        date.isEqual(currentDate.minusDays(1)) -> "KEMARIN"
        else -> date.format(uiDateFormatter).uppercase()
    }

    val categoryIcon = when (report.category.lowercase()) {
        "banjir" -> ImageVector.vectorResource(id = R.drawable.ic_flood)
        "longsor" -> ImageVector.vectorResource(id = R.drawable.ic_landslide)
        "gempa", "gempa bumi" -> ImageVector.vectorResource(id = R.drawable.ic_volcano)
        else -> ImageVector.vectorResource(id = R.drawable.ic_globe_question)
    }
    val categoryColor = when (report.category.lowercase()) {
        "banjir" -> Color(0xFF1967D2)
        "longsor" -> Color(0xFFEA8600)
        "gempa", "gempa bumi" -> Color(0xFFD93025)
        else -> Color(0xFFD93025)
    }
    val categorySurfaceColor = when (report.category.lowercase()) {
        "banjir" -> Color(0xFFE8F0FE)
        "longsor" -> Color(0xFFFEF7E0)
        else -> Color(0xFFFCE8E6)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PantauBumiColors.Green600.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = report.authorName.take(1).uppercase(),
                            color = PantauBumiColors.Green600,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = report.authorName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = reportDate,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Laporkan Tidak Relevan") },
                            onClick = {
                                onFlag()
                                showMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = categorySurfaceColor
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier
                                .size(14.dp)
                        )
                        Text(
                            text = report.category,
                            color = when (report.category) {
                                "Banjir" -> Color(0xFF1967D2)
                                "Longsor" -> Color(0xFFEA8600)
                                else -> Color(0xFFD93025)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (report.verified) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE6F4EA)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF1E8E3E),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI Terverifikasi",
                                color = Color(0xFF1E8E3E),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = report.text,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            report.imageUrl?.let { url ->
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    painter = rememberAsyncImagePainter(url),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
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
fun ReportScreenPreview() {
    val sampleReports = listOf(
        Report(
            id = 1,
            lat = -6.2,
            lng = 106.8,
            text = "Air mulai masuk rumah, ketinggian semata kaki. Mohon bantuan koordinasi di wilayah RT 04.",
            category = "Banjir",
            verified = true,
            verificationScore = 0.95,
            source = "Community",
            flagCount = 0,
            createdAt = "2023-10-26T08:00:00Z",
            authorName = "Budi S.",
            imageUrl = "https://images.unsplash.com/photo-1547683905-f686c993aae5?q=80&w=1000&auto=format&fit=crop"
        ),
        Report(
            id = 2,
            lat = -6.21,
            lng = 106.81,
            text = "Pohon tumbang di jalan utama karena tanah amblas sedikit. Lalu lintas terhambat.",
            category = "Longsor",
            verified = false,
            verificationScore = 0.4,
            source = "Community",
            flagCount = 0,
            createdAt = "2023-10-26T16:00:00Z",
            authorName = "Siti R."
        )
    )

    val state = ReportUiState(
        reports = sampleReports,
        selectedFilter = HazardType.ALL
    )

    PantauBumiTheme {
        ReportContent(
            state = state,
            isDarkTheme = false,
            onFilterSelected = {},
            onFlagReport = {},
            onAddReportClick = {},
            onDismissDialog = {},
            onSubmitReport = { _, _, _ -> },
            onRefresh = {},
            onNavigate = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    heightDp = 1080
)
@Composable
fun ReportScreenDarkPreview() {
    val sampleReports = listOf(
        Report(
            id = 1,
            lat = -6.2,
            lng = 106.8,
            text = "Air mulai masuk rumah, ketinggian semata kaki. Mohon bantuan koordinasi di wilayah RT 04.",
            category = "Banjir",
            verified = true,
            verificationScore = 0.95,
            source = "Community",
            flagCount = 0,
            createdAt = "2023-10-26T08:00:00Z",
            authorName = "Budi S.",
            imageUrl = "https://images.unsplash.com/photo-1547683905-f686c993aae5?q=80&w=1000&auto=format&fit=crop"
        ),
        Report(
            id = 2,
            lat = -6.21,
            lng = 106.81,
            text = "Pohon tumbang di jalan utama karena tanah amblas sedikit. Lalu lintas terhambat.",
            category = "Longsor",
            verified = false,
            verificationScore = 0.4,
            source = "Community",
            flagCount = 0,
            createdAt = "2023-10-26T16:00:00Z",
            authorName = "Siti R."
        )
    )

    val state = ReportUiState(
        reports = sampleReports,
        selectedFilter = HazardType.ALL
    )

    PantauBumiTheme {
        ReportContent(
            state = state,
            isDarkTheme = true,
            onFilterSelected = {},
            onFlagReport = {},
            onAddReportClick = {},
            onDismissDialog = {},
            onSubmitReport = { _, _, _ -> },
            onRefresh = {},
            onNavigate = {}
        )
    }
}
