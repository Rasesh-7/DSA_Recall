package com.example.dsarecall.ui.bank

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.dsarecall.domain.model.Difficulty
import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.SourceSheet
import com.example.dsarecall.ui.components.DifficultyChip
import com.example.dsarecall.ui.components.GlassCard
import com.example.dsarecall.ui.components.MemoryDecayIndicator
import com.example.dsarecall.ui.components.TopicTagRow
import com.example.dsarecall.ui.log.RecallLogBottomSheet
import com.example.dsarecall.ui.theme.MinimalistBackground
import com.example.dsarecall.ui.theme.MinimalistBorder
import com.example.dsarecall.ui.theme.MinimalistSurfaceVariant
import com.example.dsarecall.ui.theme.SapphirePrimary
import com.example.dsarecall.ui.theme.TextMuted
import com.example.dsarecall.ui.theme.TextPrimary
import com.example.dsarecall.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemBankScreen(
    viewModel: ProblemBankViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var selectedProblemForReader by remember { mutableStateOf<Problem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Curated Problem Sheets 📚",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search NeetCode 150 or Striver's Sheet...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextMuted
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MinimalistSurfaceVariant,
                    unfocusedContainerColor = MinimalistSurfaceVariant,
                    focusedBorderColor = SapphirePrimary,
                    unfocusedBorderColor = MinimalistBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Sheet Filter Selector (All, NeetCode 150, Striver A2Z)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sheetOptions = listOf(
                    null to "All Sheets",
                    SourceSheet.NEETCODE_150 to "NeetCode 150",
                    SourceSheet.STRIVER_A2Z to "Striver's A2Z"
                )
                sheetOptions.forEach { (sheetEnum, label) ->
                    val isSelected = state.selectedCurationSheet == sheetEnum
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected && sheetEnum != null) {
                                viewModel.onCurationSheetSelected(null)
                            } else {
                                viewModel.onCurationSheetSelected(sheetEnum)
                            }
                        },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SapphirePrimary,
                            selectedLabelColor = MinimalistBackground,
                            containerColor = MinimalistSurfaceVariant,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            // Difficulty Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedDifficultyFilter == null,
                    onClick = { viewModel.onDifficultyFilterSelected(null) },
                    label = { Text("All Difficulties") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SapphirePrimary,
                        selectedLabelColor = MinimalistBackground,
                        containerColor = MinimalistSurfaceVariant,
                        labelColor = TextSecondary
                    )
                )

                Difficulty.values().forEach { diff ->
                    FilterChip(
                        selected = state.selectedDifficultyFilter == diff,
                        onClick = {
                            if (state.selectedDifficultyFilter == diff) {
                                viewModel.onDifficultyFilterSelected(null)
                            } else {
                                viewModel.onDifficultyFilterSelected(diff)
                            }
                        },
                        label = { Text(diff.getDisplayName()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SapphirePrimary,
                            selectedLabelColor = MinimalistBackground,
                            containerColor = MinimalistSurfaceVariant,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SapphirePrimary)
                }
            } else if (state.problems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No problems match your sheet search criteria.",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.problems, key = { it.id }) { problem ->
                        ProblemBankItemCard(
                            problem = problem,
                            onCardClick = { selectedProblemForReader = problem }
                        )
                    }
                }
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

            // Recall Log Sheet Modal
            state.selectedProblemForLogging?.let { problem ->
                RecallLogBottomSheet(
                    problem = problem,
                    onDismiss = { viewModel.closeRecallLogSheet() },
                    onSubmitAttempt = { attempt -> viewModel.submitRecallAttempt(attempt) }
                )
            }
        }
    }
}

@Composable
fun ProblemBankItemCard(
    problem: Problem,
    onCardClick: () -> Unit,
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
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                DifficultyChip(difficulty = problem.difficulty)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = problem.title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            TopicTagRow(tags = problem.topicTags)

            Spacer(modifier = Modifier.height(10.dp))

            MemoryDecayIndicator(problem = problem)
        }
    }
}
