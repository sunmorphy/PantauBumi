package com.andikas.pantaubumi.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andikas.pantaubumi.ui.theme.PantauBumiColors
import com.andikas.pantaubumi.ui.theme.PantauBumiTheme

@Composable
fun PantauBumiHeader(
    locationName: String = "Jakarta Selatan",
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onToggleTheme: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(PantauBumiColors.Green600.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PantauBumiColors.Green400),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = PantauBumiColors.Green50
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "LOKASI ANDA",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    locationName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
        AnimatedContent(
            targetState = isDarkTheme,
            label = "IconChange"
        ) { targetState ->
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (targetState) Icons.Outlined.WbSunny else Icons.Outlined.DarkMode,
                    contentDescription = "Toggle Theme",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PantauBumiSecondaryHeader(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onNavigateBack: () -> Unit = {},
    onToggleTheme: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(PantauBumiColors.Green600.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PantauBumiColors.Green400)
                .clickable { onNavigateBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ArrowBackIosNew,
                contentDescription = null,
                tint = PantauBumiColors.Green50
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "Pengaturan",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        AnimatedContent(
            targetState = isDarkTheme,
            label = "IconChange"
        ) { targetState ->
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (targetState) Icons.Default.WbSunny else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun PantauBumiHeaderPreview() {
    PantauBumiTheme {
        Surface {
            PantauBumiHeader()
        }
    }
}

@Preview(showBackground = true, name = "Long Location")
@Composable
fun PantauBumiHeaderLongLocationPreview() {
    PantauBumiTheme {
        Surface {
            PantauBumiHeader(locationName = "Kota Administrasi Jakarta Selatan, DKI Jakarta")
        }
    }
}
