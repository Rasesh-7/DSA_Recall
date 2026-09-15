package com.example.dsarecall.ui.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsarecall.data.repository.ProblemRepositoryImpl
import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.RecallAttempt
import com.example.dsarecall.domain.model.TopicTag
import com.example.dsarecall.domain.repository.ProblemRepository
import com.example.dsarecall.domain.usecase.GetDailyQueueUseCase
import com.example.dsarecall.domain.usecase.LogRecallAttemptUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import com.example.dsarecall.domain.repository.AuthRepository
import com.example.dsarecall.domain.sync.SyncEngine

import com.example.dsarecall.data.repository.OnboardingRepository

data class DailyQueueUiState(
    val dueProblems: List<Problem> = emptyList(),
    val selectedTopicFilter: TopicTag? = null,
    val selectedProblemForLogging: Problem? = null,
    val isLoading: Boolean = true,
    val totalDueTodayCount: Int = 0,
    val dailyQuota: Int = 3
)

class DailyQueueViewModel(
    private val getDailyQueueUseCase: GetDailyQueueUseCase,
    private val logRecallAttemptUseCase: LogRecallAttemptUseCase,
    private val repository: ProblemRepository,
    private val syncEngine: SyncEngine? = null,
    private val authRepository: AuthRepository? = null,
    private val onboardingRepository: OnboardingRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyQueueUiState())
    val uiState: StateFlow<DailyQueueUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val quota = onboardingRepository?.getDailyQuota() ?: 3
            _uiState.value = _uiState.value.copy(dailyQuota = quota)

            if (repository is ProblemRepositoryImpl) {
                repository.seedInitialDataIfEmpty()
            }
            repository.ensureStarterProblemsTracked(count = quota)
            observeDueProblems()
        }
    }

    fun addStarterProblems(count: Int? = null) {
        viewModelScope.launch {
            val batchCount = count ?: _uiState.value.dailyQuota
            repository.activateMoreStarterProblems(count = batchCount)
        }
    }

    private fun observeDueProblems() {
        viewModelScope.launch {
            getDailyQueueUseCase(_uiState.value.selectedTopicFilter).collectLatest { dueProblems ->
                _uiState.value = _uiState.value.copy(
                    dueProblems = dueProblems,
                    totalDueTodayCount = dueProblems.size,
                    isLoading = false
                )
            }
        }
    }

    fun filterByTopic(topicTag: TopicTag?) {
        _uiState.value = _uiState.value.copy(selectedTopicFilter = topicTag)
        observeDueProblems()
    }

    fun toggleProblemTracking(problemId: String, isTracking: Boolean) {
        viewModelScope.launch {
            repository.setProblemTracking(problemId, isTracking)
            triggerAutoSync()
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
            triggerAutoSync()
        }
    }

    private fun triggerAutoSync() {
        val user = authRepository?.currentUser
        if (user != null && syncEngine != null) {
            viewModelScope.launch {
                syncEngine.performSync(user.id)
            }
        }
    }
}
