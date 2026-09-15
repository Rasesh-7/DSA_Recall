package com.example.dsarecall.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsarecall.domain.model.SourceSheet
import com.example.dsarecall.ui.components.DifficultyChip
import com.example.dsarecall.ui.theme.DifficultyEasy
import com.example.dsarecall.ui.theme.DifficultyHard
import com.example.dsarecall.ui.theme.DifficultyMedium
import com.example.dsarecall.ui.theme.MinimalistBackground
import com.example.dsarecall.ui.theme.MinimalistBorder
import com.example.dsarecall.ui.theme.MinimalistSurface
import com.example.dsarecall.ui.theme.MinimalistSurfaceVariant
import com.example.dsarecall.ui.theme.SapphirePrimary
import com.example.dsarecall.ui.theme.SolitaryGreen
import com.example.dsarecall.ui.theme.TextMuted
import com.example.dsarecall.ui.theme.TextPrimary
import com.example.dsarecall.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onCompleteOnboarding: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "DSA Recall Setup (${state.currentStep + 1}/4)",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (state.currentStep > 0 && state.currentStep < 3) {
                        IconButton(onClick = { viewModel.goToPreviousStep() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
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
                .padding(horizontal = 20.dp)
        ) {
            // Progress Bar
            LinearProgressIndicator(
                progress = (state.currentStep + 1) / 4f,
                color = SapphirePrimary,
                trackColor = MinimalistSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } with slideOutHorizontally { width -> -width }
                    } else {
                        slideInHorizontally { width -> -width } with slideOutHorizontally { width -> width }
                    }
                },
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    0 -> StepGoalSelection(state = state, viewModel = viewModel)
                    1 -> StepPriorPracticeCheckoff(state = state, viewModel = viewModel)
                    2 -> StepDiagnosticQuiz(state = state, viewModel = viewModel)
                    3 -> StepBaselineResults(state = state, viewModel = viewModel, onComplete = {
                        viewModel.completeOnboarding(onCompleteOnboarding)
                    })
                }
            }
        }
    }
}

@Composable
fun StepGoalSelection(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Welcome to DSA Recall 🧠",
            color = SapphirePrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Choose Your Target Sheet & Preparation Goal",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We will calibrate your daily spaced-repetition queue based on your sheet choice.",
            color = TextMuted,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Select Primary Sheet Focus:",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))

        val sheets = listOf(
            SourceSheet.NEETCODE_150 to "NeetCode 150 (Fast-track interview prep — 150 core patterns)",
            SourceSheet.STRIVER_A2Z to "Striver's A2Z Sheet (Comprehensive foundation to advanced — 471 problems)"
        )

        sheets.forEach { (sheet, desc) ->
            val isSelected = state.selectedSheet == sheet
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) SapphirePrimary.copy(alpha = 0.12f) else MinimalistSurface
                ),
                border = BorderStroke(1.dp, if (isSelected) SapphirePrimary else MinimalistBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { viewModel.selectSheet(sheet) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) SapphirePrimary else MinimalistSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sheet.displayName,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SapphirePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Select Goal Timeline:",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))

        val timelines = listOf(
            "INTERVIEWS" to "Upcoming Interviews (1–2 months)",
            "PLACEMENT" to "Campus Placements / Semester (3–6 months)",
            "MASTERY" to "Long-term Spaced Mastery (Continuous)"
        )

        timelines.forEach { (key, label) ->
            val isSelected = state.selectedTimeline == key
            Surface(
                onClick = { viewModel.selectTimeline(key) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) SapphirePrimary.copy(alpha = 0.12f) else MinimalistSurfaceVariant,
                border = BorderStroke(1.dp, if (isSelected) SapphirePrimary else MinimalistBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .height(48.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Select Daily Problem Pace:",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))

        val quotas = listOf(
            Triple(3, "🟢 Paced (3 cards/day)", "Light 10-min daily review — Recommended for busy schedules"),
            Triple(5, "🟣 Balanced (5 cards/day)", "Standard 20-min daily review — Optimal memory retention balance"),
            Triple(10, "⚡ Intensive (10 cards/day)", "Accelerated prep — Interview crunch mode")
        )

        quotas.forEach { (quota, title, subtitle) ->
            val isSelected = state.dailyQuota == quota
            Surface(
                onClick = { viewModel.selectDailyQuota(quota) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) SapphirePrimary.copy(alpha = 0.12f) else MinimalistSurfaceVariant,
                border = BorderStroke(1.dp, if (isSelected) SapphirePrimary else MinimalistBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SapphirePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { viewModel.goToNextStep() },
            colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Continue to Prior Practice Check-off",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun StepPriorPracticeCheckoff(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Step 2: Prior Practice Check-off 📝",
            color = SapphirePrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Have you practiced any of these core problems?",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Tap to check off what you ALREADY know so we don't mark un-started problems due today!",
            color = TextMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        state.sampleProblems.forEach { problem ->
            val isKnown = state.knownProblemIds.contains(problem.id)
            val isReview = state.needReviewProblemIds.contains(problem.id)

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isKnown -> SolitaryGreen.copy(alpha = 0.12f)
                        isReview -> SapphirePrimary.copy(alpha = 0.12f)
                        else -> MinimalistSurface
                    }
                ),
                border = BorderStroke(
                    1.dp,
                    when {
                        isKnown -> SolitaryGreen
                        isReview -> SapphirePrimary
                        else -> MinimalistBorder
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = problem.title,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        DifficultyChip(difficulty = problem.difficulty)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            onClick = { viewModel.toggleProblemKnown(problem.id) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isKnown) SolitaryGreen else MinimalistSurfaceVariant,
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isKnown) "✓ Know Well (+21d)" else "Know Well",
                                    color = if (isKnown) Color.White else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            onClick = { viewModel.toggleProblemNeedReview(problem.id) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isReview) SapphirePrimary else MinimalistSurfaceVariant,
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isReview) "✓ Review Soon (+1d)" else "Needs Review",
                                    color = if (isReview) MaterialTheme.colorScheme.onPrimary else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.goToNextStep() },
            colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Continue to 1-Min Diagnostic Quiz",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun StepDiagnosticQuiz(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Step 3: Pattern Diagnostic Quiz ⚡",
            color = SapphirePrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Quick Pattern Recall Calibration",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Answer 3 quick pattern questions to calibrate your baseline memory score.",
            color = TextMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        state.quizQuestions.forEachIndexed { qIdx, question ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalistSurface),
                border = BorderStroke(1.dp, MinimalistBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Question ${qIdx + 1}: ${question.question}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    question.options.forEachIndexed { oIdx, optionText ->
                        val selectedOpt = state.selectedQuizAnswers[question.id]
                        val isSelected = selectedOpt == oIdx

                        Surface(
                            onClick = { viewModel.selectQuizAnswer(question.id, oIdx) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SapphirePrimary.copy(alpha = 0.15f) else MinimalistSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) SapphirePrimary else MinimalistBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .height(44.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = optionText,
                                    color = if (isSelected) SapphirePrimary else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = SapphirePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.goToNextStep() },
            colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Calculate My Baseline Score",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun StepBaselineResults(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SapphirePrimary.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = SapphirePrimary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Baseline Calibration Ready! 🎉",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your personalized DSA spaced repetition engine has been calibrated.",
            color = TextMuted,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MinimalistSurface),
            border = BorderStroke(1.dp, SapphirePrimary.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "INITIAL RECALL BASELINE",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${state.baselineScorePercentage}%",
                    color = SapphirePrimary,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Target Sheet", color = TextMuted, fontSize = 11.sp)
                        Text(text = state.selectedSheet.displayName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Tracked Problems", color = TextMuted, fontSize = 11.sp)
                        Text(text = "${state.knownProblemIds.size + state.needReviewProblemIds.size}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onComplete,
            colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(imageVector = Icons.Default.RocketLaunch, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Enter My Spaced Revision Queue 🚀",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}
