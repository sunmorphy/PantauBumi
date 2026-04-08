package com.andikas.pantaubumi.ui.onboarding

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andikas.pantaubumi.R
import com.andikas.pantaubumi.ui.components.PantauBumiButton
import com.andikas.pantaubumi.ui.components.PantauBumiTextButton
import com.andikas.pantaubumi.ui.theme.PantauBumiColors
import com.andikas.pantaubumi.ui.theme.PantauBumiTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int,
    val buttonText: String
)

val onboardingPages = listOf(
    OnboardingPage(
        title = "Pantau risiko bencana di sekitarmu secara real-time.",
        description = "Dapatkan peringatan dini dan informasi terkini untuk keamanan Anda dan keluarga.",
        imageRes = R.drawable.ic_boarding_1,
        buttonText = "Lanjutkan"
    ),
    OnboardingPage(
        title = "Dapatkan peringatan sebelum banjir, longsor, atau gempa terjadi.",
        description = "Sistem deteksi dini kami memberikan informasi real-time untuk menjaga keselamatan Anda dan keluarga.",
        imageRes = R.drawable.ic_boarding_2,
        buttonText = "Lanjutkan"
    ),
    OnboardingPage(
        title = "Tetap aman, bahkan tanpa sinyal internet.",
        description = "PantauBumi dirancang untuk bekerja dalam kondisi tersulit sekalipun dengan peta offline yang selalu siap.",
        imageRes = R.drawable.ic_boarding_3,
        buttonText = "Mulai Sekarang"
    )
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    val permissionsList = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionsList.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    val permissionState = rememberMultiplePermissionsState(permissionsList)

    var showRationale by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var userTriggeredPermission by remember { mutableStateOf(false) }

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (userTriggeredPermission && permissionState.allPermissionsGranted) {
            onFinished()
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Izin Akses Diperlukan") },
            text = { Text("PantauBumi sangat membutuhkan izin Lokasi dan Notifikasi agar dapat memberikan peringatan bencana di sekitar Anda secara akurat.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    userTriggeredPermission = true
                    permissionState.launchMultiplePermissionRequest()
                }) { Text("Izinkan") }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false; onFinished() }) { Text("Lewati") }
            }
        )
    }

    if (showSettingsDialog && !permissionState.shouldShowRationale && !permissionState.allPermissionsGranted) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Izin Ditolak Permanen") },
            text = { Text("Anda telah menolak izin secara permanen. Mohon buka Pengaturan aplikasi untuk mengaktifkannya secara manual agar aplikasi dapat berfungsi optimal.") },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                    onFinished()
                }) { Text("Buka Pengaturan") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSettingsDialog = false; onFinished()
                }) { Text("Lewati") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_transparent),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )

            PantauBumiTextButton(
                text = "Lewati",
                onClick = onFinished
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) { pageIndex ->
            val page = onboardingPages[pageIndex]
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(PantauBumiColors.Earth100),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = page.imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(200.dp)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 36.sp,
                        textAlign = TextAlign.Center
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier.height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(onboardingPages.size) { iteration ->
                val color =
                    if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onPrimaryContainer
                val width = if (pagerState.currentPage == iteration) 32.dp else 8.dp
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(width = width, height = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        PantauBumiButton(
            text = onboardingPages[pagerState.currentPage].buttonText,
            onClick = {
                if (pagerState.currentPage < onboardingPages.size - 1) {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    if (permissionState.allPermissionsGranted) {
                        onFinished()
                    } else if (permissionState.shouldShowRationale) {
                        showRationale = true
                    } else if (userTriggeredPermission && !permissionState.allPermissionsGranted) {
                        showSettingsDialog = true
                    } else {
                        showRationale = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.AutoMirrored.Filled.ArrowForward
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light Mode"
)
@Composable
fun OnboardingScreenPreview() {
    PantauBumiTheme {
        OnboardingScreen(onFinished = {})
    }
}


@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun OnboardingScreenDarkPreview() {
    PantauBumiTheme {
        OnboardingScreen(onFinished = {})
    }
}
