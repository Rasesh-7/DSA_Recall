package com.example.dsarecall.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsarecall.data.repository.OnboardingRepository
import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.SourceSheet
import com.example.dsarecall.domain.repository.ProblemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class OnboardingQuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

data class OnboardingUiState(
    val currentStep: Int = 0, // 0: Goal, 1: Checkoff, 2: Quiz, 3: Baseline Result
    val selectedSheet: SourceSheet = SourceSheet.NEETCODE_150,
    val selectedTimeline: String = "INTERVIEWS", // "INTERVIEWS", "PLACEMENT", "MASTERY"
    val sampleProblems: List<Problem> = emptyList(),
    val knownProblemIds: Set<String> = emptySet(),
    val needReviewProblemIds: Set<String> = emptySet(),
    val quizQuestions: List<OnboardingQuizQuestion> = emptyList(),
    val selectedQuizAnswers: Map<Int, Int> = emptyMap(),
    val quizScore: Int = 0,
    val baselineScorePercentage: Int = 0,
    val isCompleted: Boolean = false
)

class OnboardingViewModel(
    private val repository: ProblemRepository,
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadSampleProblems()
        setupQuizQuestions()
    }

    private fun loadSampleProblems() {
        viewModelScope.launch {
            repository.observeAllProblems().collect { problems ->
                val samples = problems.take(12)
                _uiState.value = _uiState.value.copy(sampleProblems = samples)
            }
        }
    }

    private fun setupQuizQuestions() {
        val questions = listOf(
            OnboardingQuizQuestion(
                id = 1,
                question = "Given an array sorted in ascending order, which technique finds a target element in O(log N) time complexity?",
                options = listOf("Sliding Window", "Binary Search", "Monotonic Stack", "Union Find"),
                correctOptionIndex = 1,
                explanation = "Binary Search repeatedly divides the sorted search space in half to achieve logarithmic O(log N) time."
            ),
            OnboardingQuizQuestion(
                id = 2,
                question = "Which data structure is optimal for finding the 'Top K Frequent Elements' in O(N log K) time?",
                options = listOf("Min-Heap / Priority Queue", "Segment Tree", "Circular Queue", "Trie"),
                correctOptionIndex = 0,
                explanation = "A Min-Heap of size K maintains the top K frequencies efficiently in O(N log K) time."
            ),
            OnboardingQuizQuestion(
                id = 3,
                question = "Which graph algorithm detects a cycle in a Directed Graph using topological sorting principles?",
                options = listOf("Kahn's Algorithm (BFS Topo Sort)", "Sliding Window", "Kruskal's MST", "Dijkstra's Algorithm"),
                correctOptionIndex = 0,
                explanation = "Kahn's Algorithm processes in-degrees; if the processed node count < V, a directed cycle exists."
            )
        )
        _uiState.value = _uiState.value.copy(quizQuestions = questions)
    }

    fun selectSheet(sheet: SourceSheet) {
        _uiState.value = _uiState.value.copy(selectedSheet = sheet)
    }

    fun selectTimeline(timeline: String) {
        _uiState.value = _uiState.value.copy(selectedTimeline = timeline)
    }

    fun toggleProblemKnown(problemId: String) {
        val currentKnown = _uiState.value.knownProblemIds.toMutableSet()
        val currentReview = _uiState.value.needReviewProblemIds.toMutableSet()

        if (currentKnown.contains(problemId)) {
            currentKnown.remove(problemId)
        } else {
            currentKnown.add(problemId)
            currentReview.remove(problemId)
        }
        _uiState.value = _uiState.value.copy(knownProblemIds = currentKnown, needReviewProblemIds = currentReview)
    }

    fun toggleProblemNeedReview(problemId: String) {
        val currentKnown = _uiState.value.knownProblemIds.toMutableSet()
        val currentReview = _uiState.value.needReviewProblemIds.toMutableSet()

        if (currentReview.contains(problemId)) {
            currentReview.remove(problemId)
        } else {
            currentReview.add(problemId)
            currentKnown.remove(problemId)
        }
        _uiState.value = _uiState.value.copy(knownProblemIds = currentKnown, needReviewProblemIds = currentReview)
    }

    fun selectQuizAnswer(questionId: Int, optionIndex: Int) {
        val currentAnswers = _uiState.value.selectedQuizAnswers.toMutableMap()
        currentAnswers[questionId] = optionIndex
        _uiState.value = _uiState.value.copy(selectedQuizAnswers = currentAnswers)
    }

    fun goToNextStep() {
        val current = _uiState.value.currentStep
        if (current == 2) {
            // Calculate Quiz Score & Baseline
            val questions = _uiState.value.quizQuestions
            val answers = _uiState.value.selectedQuizAnswers
            var score = 0
            questions.forEach { q ->
                if (answers[q.id] == q.correctOptionIndex) {
                    score++
                }
            }
            val percentage = ((score.toDouble() / questions.size) * 100).toInt().coerceIn(40, 100)
            _uiState.value = _uiState.value.copy(
                quizScore = score,
                baselineScorePercentage = percentage,
                currentStep = 3
            )
        } else if (current < 3) {
            _uiState.value = _uiState.value.copy(currentStep = current + 1)
        }
    }

    fun goToPreviousStep() {
        val current = _uiState.value.currentStep
        if (current > 0) {
            _uiState.value = _uiState.value.copy(currentStep = current - 1)
        }
    }

    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 3600 * 1000L

            // 1. Bulk update Known problems (tracking = true, nextDueDate = +14 days)
            val knownIds = _uiState.value.knownProblemIds.toList()
            if (knownIds.isNotEmpty()) {
                repository.bulkUpdateTracking(knownIds, isTracking = true, initialDueDate = now + 14 * dayMs)
            }

            // 2. Bulk update Need Review problems (tracking = true, nextDueDate = now, due immediately today!)
            val reviewIds = _uiState.value.needReviewProblemIds.toList()
            if (reviewIds.isNotEmpty()) {
                repository.bulkUpdateTracking(reviewIds, isTracking = true, initialDueDate = now)
            }

            // 3. Ensure starter problems for chosen sheet are active & due today
            repository.ensureStarterProblemsTracked(targetSheet = _uiState.value.selectedSheet, count = 15)

            // 4. Save Onboarding preferences
            onboardingRepository.saveUserPreferences(
                targetSheet = _uiState.value.selectedSheet.name,
                goalTimeline = _uiState.value.selectedTimeline,
                baselineScore = _uiState.value.baselineScorePercentage
            )
            onboardingRepository.setOnboardingCompleted(true)
            _uiState.value = _uiState.value.copy(isCompleted = true)

            onFinished()
        }
    }
}
