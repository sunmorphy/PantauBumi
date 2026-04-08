package com.andikas.pantaubumi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andikas.pantaubumi.data.local.PrefManager
import com.andikas.pantaubumi.domain.repository.PantauBumiRepository
import com.andikas.pantaubumi.ui.alerts.AlertsScreen
import com.andikas.pantaubumi.ui.components.OfflineIndicator
import com.andikas.pantaubumi.ui.dashboard.DashboardScreen
import com.andikas.pantaubumi.ui.map.MapScreen
import com.andikas.pantaubumi.ui.onboarding.OnboardingScreen
import com.andikas.pantaubumi.ui.reports.ReportScreen
import com.andikas.pantaubumi.ui.settings.SettingsScreen
import com.andikas.pantaubumi.ui.theme.PantauBumiTheme
import com.andikas.pantaubumi.util.NetworkMonitor
import com.andikas.pantaubumi.util.NetworkStatus
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefManager: PrefManager

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var repository: PantauBumiRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val isOnboardingCompleted by prefManager.isOnboardingCompleted.collectAsState(initial = null)
            val isDarkModePref by prefManager.isDarkMode.collectAsState(initial = null)
            val networkStatus by networkMonitor.status.collectAsStateWithLifecycle(initialValue = NetworkStatus.Available)
            val scope = rememberCoroutineScope()
            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        scope.launch {
                            prefManager.setFcmToken(token)
                            repository.updateFcmToken(token, prefManager.deviceId)
                        }
                    }
                }
            }

            val isDarkTheme = isDarkModePref ?: isSystemInDarkTheme()
            val toggleTheme = {
                scope.launch {
                    prefManager.setDarkMode(!isDarkTheme)
                }
            }

            println("kontol ${BuildConfig.BASE_URL}")

            PantauBumiTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                        .padding(top = 8.dp),
                    topBar = {
                        OfflineIndicator(
                            isOffline = networkStatus == NetworkStatus.Unavailable,
                            modifier = Modifier.statusBarsPadding()
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (isOnboardingCompleted) {
                            null -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            false -> {
                                OnboardingScreen(
                                    onFinished = {
                                        scope.launch {
                                            prefManager.setOnboardingCompleted(true)
                                        }
                                    }
                                )
                            }

                            else -> {
                                val onNavigate: (String) -> Unit = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }

                                NavHost(
                                    navController = navController,
                                    startDestination = "dashboard"
                                ) {
                                    composable("dashboard") {
                                        DashboardScreen(
                                            isDarkTheme = isDarkTheme,
                                            onToggleTheme = { toggleTheme() },
                                            onNavigate = onNavigate
                                        )
                                    }
                                    composable("alerts") {
                                        AlertsScreen(
                                            isDarkTheme = isDarkTheme,
                                            onToggleTheme = { toggleTheme() },
                                            onNavigate = onNavigate
                                        )
                                    }
                                    composable("report") {
                                        ReportScreen(
                                            isDarkTheme = isDarkTheme,
                                            onToggleTheme = { toggleTheme() },
                                            onNavigate = onNavigate
                                        )
                                    }
                                    composable(
                                        route = "map?evacuationId={evacuationId}",
                                        arguments = listOf(
                                            androidx.navigation.navArgument("evacuationId") {
                                                type = androidx.navigation.NavType.IntType
                                                defaultValue = -1
                                            }
                                        )
                                    ) { backStackEntry ->
                                        val evacuationId =
                                            backStackEntry.arguments?.getInt("evacuationId") ?: -1
                                        MapScreen(
                                            isDarkTheme = isDarkTheme,
                                            onToggleTheme = { toggleTheme() },
                                            onNavigate = onNavigate,
                                            targetEvacuationId = evacuationId
                                        )
                                    }
                                    composable("settings") {
                                        SettingsScreen(
                                            isDarkTheme = isDarkTheme,
                                            onToggleTheme = { toggleTheme() },
                                            onNavigateBack = { navController.popBackStack() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
