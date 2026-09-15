package com.example.dsarecall.ui.analytics

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.dsarecall.ui.components.GlassCard
import com.example.dsarecall.ui.theme.DifficultyEasy
import com.example.dsarecall.ui.theme.DifficultyHard
import com.example.dsarecall.ui.theme.DifficultyMedium
import com.example.dsarecall.ui.theme.MauvePrimary
import com.example.dsarecall.ui.theme.MinimalistBackground
import com.example.dsarecall.ui.theme.MinimalistSurfaceVariant
import com.example.dsarecall.ui.theme.SapphirePrimary
import com.example.dsarecall.ui.theme.SoftLavender
import com.example.dsarecall.ui.theme.TextMuted
import com.example.dsarecall.ui.theme.TextPrimary
import com.example.dsarecall.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Memory Analytics & Retention 📊",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MinimalistBackground)
            )
        },
        containerColor = MinimalistBackground
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SapphirePrimary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 1. Placement Readiness Index Card
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = "Placement Readiness Index",
                                        color = SoftLavender.copy(alpha = 0.7f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${state.placementReadinessIndex}%",
                                        color = MauvePrimary,
                                        fontSize = 34.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = MauvePrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { state.placementReadinessIndex / 100f },
                                color = MauvePrimary,
                                trackColor = Color(0xFF251F33),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(9999.dp))
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Independent Solves: ${state.solitarySolvedRatio}%",
                                    color = SoftLavender,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Total Problems: ${state.totalProblems}",
                                    color = SoftLavender,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // 2. Topic Stability Matrix
                item {
                    Text(
                        text = "Topic Weakness & Stability Matrix",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Topics sorted from lowest to highest memory stability.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                items(state.topicStabilities) { topicStability ->
                    val color = when {
                        topicStability.stabilityPercentage < 50 -> DifficultyHard
                        topicStability.stabilityPercentage < 75 -> DifficultyMedium
                        else -> DifficultyEasy
                    }

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = topicStability.topicTag.displayName,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "${topicStability.stabilityPercentage}% Memory Retention",
                                    color = color,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { topicStability.stabilityPercentage / 100f },
                                color = color,
                                trackColor = MinimalistSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${topicStability.totalProblems} problem(s) tracked • Avg Ease: ${String.format("%.2f", topicStability.averageEaseFactor)}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
