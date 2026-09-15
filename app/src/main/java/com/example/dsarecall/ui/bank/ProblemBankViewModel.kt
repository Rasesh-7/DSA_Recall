package com.example.dsarecall.ui.bank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsarecall.data.repository.ProblemRepositoryImpl
import com.example.dsarecall.domain.model.Difficulty
import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.RecallAttempt
import com.example.dsarecall.domain.model.SourceSheet
import com.example.dsarecall.domain.model.TopicTag
import com.example.dsarecall.domain.repository.ProblemRepository
import com.example.dsarecall.domain.usecase.GetProblemBankUseCase
import com.example.dsarecall.domain.usecase.LogRecallAttemptUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class ProblemBankUiState(
    val problems: List<Problem> = emptyList(),
    val searchQuery: String = "",
    val selectedCurationSheet: SourceSheet? = null, // null = All, NEETCODE_150, STRIVER_A2Z
    val selectedTopicFilter: TopicTag? = null,
    val selectedDifficultyFilter: Difficulty? = null,
    val selectedProblemForLogging: Problem? = null,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProblemBankViewModel(
    private val getProblemBankUseCase: GetProblemBankUseCase,
    private val logRecallAttemptUseCase: LogRecallAttemptUseCase,
    private val repository: ProblemRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedSheet = MutableStateFlow<SourceSheet?>(null)
    private val _selectedTopic = MutableStateFlow<TopicTag?>(null)
    private val _selectedDifficulty = MutableStateFlow<Difficulty?>(null)

    private val _uiState = MutableStateFlow(ProblemBankUiState())
    val uiState: StateFlow<ProblemBankUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (repository is ProblemRepositoryImpl) {
                repository.seedInitialDataIfEmpty()
            }

            combine(
                _searchQuery,
                _selectedSheet,
                _selectedTopic,
                _selectedDifficulty
            ) { query, sheet, topic, difficulty ->
                FilterParams(query, sheet, topic, difficulty)
            }.flatMapLatest { params ->
                getProblemBankUseCase(params.query, params.topic, params.difficulty, params.sheet)
                    .map { filteredProblems ->
                        ProblemBankUiState(
                            problems = filteredProblems,
                            searchQuery = params.query,
                            selectedCurationSheet = params.sheet,
                            selectedTopicFilter = params.topic,
                            selectedDifficultyFilter = params.difficulty,
                            isLoading = false
                        )
                    }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCurationSheetSelected(sheet: SourceSheet?) {
        _selectedSheet.value = sheet
    }

    fun onTopicFilterSelected(topic: TopicTag?) {
        _selectedTopic.value = topic
    }

    fun onDifficultyFilterSelected(difficulty: Difficulty?) {
        _selectedDifficulty.value = difficulty
    }

    fun toggleProblemTracking(problemId: String, isTracking: Boolean) {
        viewModelScope.launch {
            repository.setProblemTracking(problemId, isTracking)
        }
    }

    fun openRecallLogSheet(problem: Problem) {
        _uiState.value = _uiState.value.copy(selectedProblemForLogging = problem)
    }

    fun closeRecallLogSheet() {
        _uiState.value = _uiState.value.copy(selectedProblemForLogging = null)
    }

    fun submitRecallAttempt(attempt: RecallAttempt) {
        val problem = _uiState.value.selectedProblemForLogging ?: return
        viewModelScope.launch {
            logRecallAttemptUseCase(problem.id, attempt)
            closeRecallLogSheet()
        }
    }

    private data class FilterParams(
        val query: String,
        val sheet: SourceSheet?,
        val topic: TopicTag?,
        val difficulty: Difficulty?
    )
}
