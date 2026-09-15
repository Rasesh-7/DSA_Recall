package com.example.dsarecall.ui.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsarecall.domain.engine.SpacedRepetitionEngine
import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.RecallAttempt
import com.example.dsarecall.domain.model.SolutionReliance
import com.example.dsarecall.ui.components.DifficultyChip
import com.example.dsarecall.ui.components.RecallRatingBar
import com.example.dsarecall.ui.components.SolutionRelianceSelector
import com.example.dsarecall.ui.theme.MinimalistBorder
import com.example.dsarecall.ui.theme.MinimalistSurface
import com.example.dsarecall.ui.theme.MinimalistSurfaceVariant
import com.example.dsarecall.ui.theme.SapphirePrimary
import com.example.dsarecall.ui.theme.TextMuted
import com.example.dsarecall.ui.theme.TextPrimary
import com.example.dsarecall.ui.theme.TextSecondary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecallLogBottomSheet(
    problem: Problem,
    onDismiss: () -> Unit,
    onSubmitAttempt: (RecallAttempt) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var selectedScore by remember { mutableStateOf(4) }
    var selectedReliance by remember { mutableStateOf(SolutionReliance.SOLVED_SOLITARY) }
    var durationMinutesText by remember { mutableStateOf("10") }
    var trickNoteText by remember { mutableStateOf(problem.latestTrickNote) }

    val tempAttempt = remember(selectedScore, selectedReliance) {
        RecallAttempt(
            id = "preview",
            problemId = problem.id,
            recallScore = selectedScore,
            solutionReliance = selectedReliance
        )
    }
    val previewResult = remember(tempAttempt) {
        SpacedRepetitionEngine.calculateNextReview(problem, tempAttempt)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MinimalistSurface
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Log Revision Attempt",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = problem.title,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                DifficultyChip(difficulty = problem.difficulty)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Recall Rating Bar
            RecallRatingBar(
                selectedRating = selectedScore,
                onRatingSelected = { selectedScore = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Solution Reliance Selector
            SolutionRelianceSelector(
                selectedReliance = selectedReliance,
                onRelianceSelected = { selectedReliance = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Time Taken (Minutes)
            Text(
                text = "Solving Duration (Minutes)",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = durationMinutesText,
                onValueChange = { durationMinutesText = it.filter { char -> char.isDigit() } },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MinimalistSurfaceVariant,
                    unfocusedContainerColor = MinimalistSurfaceVariant,
                    focusedBorderColor = SapphirePrimary,
                    unfocusedBorderColor = MinimalistBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. "The Key Trick / Intuition" Markdown Note
            Text(
                text = "Key Intuition / Pattern Trick (Markdown)",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Write a 1-line summary of the core trick to review before future solves.",
                color = TextMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = trickNoteText,
                onValueChange = { trickNoteText = it },
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MinimalistSurfaceVariant,
                    unfocusedContainerColor = MinimalistSurfaceVariant,
                    focusedBorderColor = SapphirePrimary,
                    unfocusedBorderColor = MinimalistBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Live Calculation Preview Badge
            Text(
                text = "💡 Next Review: In ${previewResult.newIntervalDays} day(s) (Ease: ${String.format("%.2f", previewResult.newEaseFactor)})",
                color = if (previewResult.isSuccess) SapphirePrimary else TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val durationSec = (durationMinutesText.toIntOrNull() ?: 10) * 60
                    val attempt = RecallAttempt(
                        id = UUID.randomUUID().toString(),
                        problemId = problem.id,
                        recallScore = selectedScore,
                        solutionReliance = selectedReliance,
                        durationSeconds = durationSec,
                        keyIntuitionNote = trickNoteText.trim()
                    )
                    onSubmitAttempt(attempt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    text = "Save Attempt & Schedule Next Review",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
