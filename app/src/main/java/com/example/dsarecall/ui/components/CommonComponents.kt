package com.example.dsarecall.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsarecall.domain.model.Difficulty
import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.SolutionReliance
import com.example.dsarecall.domain.model.TopicTag
import com.example.dsarecall.ui.theme.DifficultyEasy
import com.example.dsarecall.ui.theme.DifficultyHard
import com.example.dsarecall.ui.theme.DifficultyMedium
import com.example.dsarecall.ui.theme.FailedRed
import com.example.dsarecall.ui.theme.HintYellow
import com.example.dsarecall.ui.theme.MinimalistBorder
import com.example.dsarecall.ui.theme.MinimalistSurface
import com.example.dsarecall.ui.theme.MinimalistSurfaceVariant
import com.example.dsarecall.ui.theme.SapphirePrimary
import com.example.dsarecall.ui.theme.SlateSecondary
import com.example.dsarecall.ui.theme.SolitaryGreen
import com.example.dsarecall.ui.theme.SolutionOrange
import com.example.dsarecall.ui.theme.TextMuted
import com.example.dsarecall.ui.theme.TextPrimary
import com.example.dsarecall.ui.theme.TextSecondary

@Composable
fun DifficultyChip(difficulty: Difficulty, modifier: Modifier = Modifier) {
    val (color, label) = when (difficulty) {
        Difficulty.EASY -> DifficultyEasy to "Easy"
        Difficulty.MEDIUM -> DifficultyMedium to "Medium"
        Difficulty.HARD -> DifficultyHard to "Hard"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TopicTagChip(tag: TopicTag, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MinimalistSurfaceVariant)
            .border(1.dp, MinimalistBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = tag.displayName,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopicTagRow(tags: List<TopicTag>, modifier: Modifier = Modifier) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        tags.forEach { tag ->
            TopicTagChip(tag = tag)
        }
    }
}

@Composable
fun MemoryDecayIndicator(problem: Problem, modifier: Modifier = Modifier) {
    val now = System.currentTimeMillis()
    val isDue = problem.isTracking && problem.nextDueDate <= now
    val daysUntilDue = ((problem.nextDueDate - now) / (24 * 3600 * 1000L)).toInt()

    val (bgColor, textColor, text) = when {
        !problem.isTracking && problem.totalAttempts == 0 -> Triple(
            TextMuted.copy(alpha = 0.12f),
            TextSecondary,
            "Not Started"
        )
        isDue && daysUntilDue < -1 -> Triple(
            FailedRed.copy(alpha = 0.12f),
            FailedRed,
            "Overdue by ${-daysUntilDue}d"
        )
        isDue -> Triple(
            HintYellow.copy(alpha = 0.12f),
            HintYellow,
            "Due Today"
        )
        else -> Triple(
            SolitaryGreen.copy(alpha = 0.12f),
            SolitaryGreen,
            "Due in ${daysUntilDue + 1}d • Int: ${problem.intervalDays}d"
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MinimalistSurface),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = listOf(
                    SapphirePrimary.copy(alpha = 0.35f),
                    MinimalistBorder,
                    SlateSecondary.copy(alpha = 0.20f)
                )
            )
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        )
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun LogRecallButton(
    onLogRecall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onLogRecall,
        colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.height(44.dp)
    ) {
        Icon(
            imageVector = Icons.Default.FactCheck,
            contentDescription = "Log Recall Check",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Checklist Recall Log",
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun RecallRatingBar(
    selectedRating: Int,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Recall Confidence Rating",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            (1..5).forEach { rating ->
                val isSelected = rating <= selectedRating
                val color by animateColorAsState(
                    targetValue = if (isSelected) SapphirePrimary else TextMuted,
                    label = "StarColor"
                )

                Surface(
                    onClick = { onRatingSelected(rating) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (rating == selectedRating) SapphirePrimary.copy(alpha = 0.15f) else MinimalistSurfaceVariant,
                    border = BorderStroke(1.dp, if (rating == selectedRating) SapphirePrimary else MinimalistBorder),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp)
                        .height(54.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating $rating",
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$rating",
                            color = if (isSelected) SapphirePrimary else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        val hintText = when (selectedRating) {
            1 -> "1 - Complete Blackout / Forgot pattern"
            2 -> "2 - Needed heavy hints & 15+ mins"
            3 -> "3 - Recalled approach after effort"
            4 -> "4 - Good recall with minor hesitation"
            5 -> "5 - Instant optimal solution recall"
            else -> "Select how well you remembered the solution"
        }
        Text(
            text = hintText,
            color = TextMuted,
            fontSize = 12.sp
        )
    }
}

@Composable
fun SolutionRelianceSelector(
    selectedReliance: SolutionReliance,
    onRelianceSelected: (SolutionReliance) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Solve Reliance Level",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SolutionReliance.values().forEach { reliance ->
                val isSelected = reliance == selectedReliance
                val borderColor = if (isSelected) {
                    when (reliance) {
                        SolutionReliance.SOLVED_SOLITARY -> SolitaryGreen
                        SolutionReliance.NEEDED_HINT -> HintYellow
                        SolutionReliance.LOOKED_AT_SOLUTION -> SolutionOrange
                        SolutionReliance.FAILED -> FailedRed
                    }
                } else MinimalistBorder

                Surface(
                    onClick = { onRelianceSelected(reliance) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) borderColor.copy(alpha = 0.12f) else MinimalistSurfaceVariant,
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    ) {
                        Text(
                            text = reliance.label,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = borderColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
