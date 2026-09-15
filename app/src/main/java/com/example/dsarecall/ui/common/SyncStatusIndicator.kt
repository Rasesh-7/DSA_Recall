package com.example.dsarecall.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsarecall.domain.model.SyncStatus
import com.example.dsarecall.ui.theme.DifficultyEasy
import com.example.dsarecall.ui.theme.SapphirePrimary
import com.example.dsarecall.ui.theme.TextMuted

@Composable
fun SyncStatusIndicator(
    syncStatus: SyncStatus,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (iconColor, label, isSpinning) = when (syncStatus) {
        is SyncStatus.Syncing -> Triple(SapphirePrimary, "Syncing...", true)
        is SyncStatus.Synced -> Triple(DifficultyEasy, "Cloud Synced", false)
        is SyncStatus.Error -> Triple(MaterialTheme.colorScheme.error, "Sync Error", false)
        is SyncStatus.Idle -> Triple(TextMuted, "Offline Backup", false)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onSyncClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isSpinning) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                color = iconColor,
                strokeWidth = 2.dp
            )
        } else {
            val icon = when (syncStatus) {
                is SyncStatus.Synced -> Icons.Default.CheckCircle
                is SyncStatus.Error -> Icons.Default.CloudOff
                else -> Icons.Default.Sync
            }
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
