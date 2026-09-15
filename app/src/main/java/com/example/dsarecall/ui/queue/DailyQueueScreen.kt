package com.example.dsarecall.ui.queue

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.TopicTag
import com.example.dsarecall.ui.bank.ProblemDetailSheet
import com.example.dsarecall.ui.components.DifficultyChip
import com.example.dsarecall.ui.components.GlassCard
import com.example.dsarecall.ui.components.LogRecallButton
import com.example.dsarecall.ui.components.MemoryDecayIndicator
import com.example.dsarecall.ui.components.TopicTagRow
import com.example.dsarecall.ui.log.RecallLogBottomSheet
import com.example.dsarecall.ui.theme.GlowingGreen
import com.example.dsarecall.ui.theme.MauvePrimary
import com.example.dsarecall.ui.theme.MinimalistBackground
import com.example.dsarecall.ui.theme.MinimalistSurfaceVariant
import com.example.dsarecall.ui.theme.ObsidianVoid
import com.example.dsarecall.ui.theme.PaleLilac
import com.example.dsarecall.ui.theme.SapphirePrimary
import com.example.dsarecall.ui.theme.SoftLavender
import com.example.dsarecall.ui.theme.SolitaryGreen
import com.example.dsarecall.ui.theme.TextMuted
import com.example.dsarecall.ui.theme.TextPrimary
import com.example.dsarecall.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyQueueScreen(
    viewModel: DailyQueueViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var selectedProblemForReader by remember { mutableStateOf<Problem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Daily Revision Checklist 🧠",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "What DSA problem should I recall today?",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MinimalistBackground)
            )
        },
        containerColor = MinimalistBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Topic Filter Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedTopicFilter == null,
                    onClick = { viewModel.filterByTopic(null) },
                    label = { Text("All (${state.totalDueTodayCount})") },
                    shape = RoundedCornerShape(9999.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MauvePrimary,
                        selectedLabelColor = ObsidianVoid,
                        containerColor = Color(0xFF251F33),
                        labelColor = SoftLavender
                    )
                )

                TopicTag.values().forEach { topic ->
                    FilterChip(
                        selected = state.selectedTopicFilter == topic,
                        onClick = {
                            if (state.selectedTopicFilter == topic) {
                                viewModel.filterByTopic(null)
                            } else {
                                viewModel.filterByTopic(topic)
                            }
                        },
                        label = { Text(topic.displayName) },
                        shape = RoundedCornerShape(9999.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MauvePrimary,
                            selectedLabelColor = ObsidianVoid,
                            containerColor = Color(0xFF251F33),
                            labelColor = SoftLavender
                        )
                    )
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SapphirePrimary)
                }
            } else if (state.dueProblems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GlowingGreen,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Daily Queue Complete! 🎉",
                            color = PaleLilac,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Awesome job! You've cleared your scheduled recall cards for today. Your memory retention intervals are updating in the background.",
                            color = SoftLavender.copy(alpha = 0.75f),
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        androidx.compose.material3.Button(
                            onClick = { viewModel.addStarterProblems(state.dailyQuota) },
                            shape = RoundedCornerShape(9999.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MauvePrimary)
                        ) {
                            Text(
                                text = "Add ${state.dailyQuota} More Questions (+${state.dailyQuota} Cards)",
                                color = ObsidianVoid,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.dueProblems, key = { it.id }) { problem ->
                        ProblemQueueItemCard(
                            problem = problem,
                            onCardClick = { selectedProblemForReader = problem },
                            onLogAttempt = { viewModel.openRecallLogSheet(problem) }
                        )
                    }
                }
            }

            // Log Sheet Modal BottomSheet
            state.selectedProblemForLogging?.let { problem ->
                RecallLogBottomSheet(
                    problem = problem,
                    onDismiss = { viewModel.closeRecallLogSheet() },
                    onSubmitAttempt = { attempt -> viewModel.submitRecallAttempt(attempt) }
                )
            }

            // In-App Problem Reader Sheet
            selectedProblemForReader?.let { problem ->
                ProblemDetailSheet(
                    problem = problem,
                    onDismiss = { selectedProblemForReader = null },
                    onToggleTracking = { isTracking ->
                        viewModel.toggleProblemTracking(problem.id, isTracking)
                        selectedProblemForReader = problem.copy(isTracking = isTracking)
                    },
                    onOpenRecallLog = {
                        val p = problem
                        selectedProblemForReader = null
                        viewModel.openRecallLogSheet(p)
                    }
                )
            }
        }
    }
}

@Composable
fun ProblemQueueItemCard(
    problem: Problem,
    onCardClick: () -> Unit,
    onLogAttempt: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        onClick = onCardClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = problem.topicTags.firstOrNull()?.displayName ?: "DSA",
                    color = SapphirePrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                DifficultyChip(difficulty = problem.difficulty)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = problem.title,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            TopicTagRow(tags = problem.topicTags)

            if (problem.latestTrickNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = SapphirePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Trick: ${problem.latestTrickNote}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                MemoryDecayIndicator(problem = problem)

                LogRecallButton(
                    onLogRecall = onLogAttempt
                )
            }
        }
    }
}
