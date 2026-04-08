package com.andikas.pantaubumi.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andikas.pantaubumi.R
import com.andikas.pantaubumi.ui.theme.PantauBumiTheme

@Composable
fun PantauBumiBottomNavigation(
    selectedRoute: String = "dashboard",
    onRouteSelected: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .padding(start = 8.dp, top = 0.dp, end = 8.dp, bottom = 16.dp)
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
        ) {
            NavigationBarItem(
                selected = selectedRoute == "dashboard",
                onClick = { onRouteSelected("dashboard") },
                icon = { Icon(Icons.Outlined.Dashboard, contentDescription = null) },
                label = { Text("Beranda") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            NavigationBarItem(
                selected = selectedRoute == "alerts",
                onClick = { onRouteSelected("alerts") },
                icon = {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.ic_crisis_alert),
                        contentDescription = null
                    )
                },
                label = { Text("Peringatan") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            NavigationBarItem(
                selected = selectedRoute == "map",
                onClick = { onRouteSelected("map") },
                icon = { Icon(Icons.Outlined.Map, contentDescription = null) },
                label = { Text("Peta") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            NavigationBarItem(
                selected = selectedRoute == "report",
                onClick = { onRouteSelected("report") },
                icon = { Icon(Icons.AutoMirrored.Outlined.ListAlt, contentDescription = null) },
                label = { Text("Laporan") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun PantauBumiBottomNavigationPreview() {
    PantauBumiTheme {
        PantauBumiBottomNavigation()
    }
}
