package com.andikas.pantaubumi.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andikas.pantaubumi.ui.theme.PantauBumiColors

@Composable
fun PantauBumiChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        label = {
            Text(
                label,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PantauBumiColors.Green600,
            selectedLabelColor = PantauBumiColors.Green200,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}