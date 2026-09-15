package com.example.dsarecall.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsarecall.domain.model.RecallAttempt
import com.example.dsarecall.domain.usecase.DomainAnalyticsData
import com.example.dsarecall.domain.usecase.GetAnalyticsUseCase
import com.example.dsarecall.domain.usecase.TopicStabilityMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val totalProblems: Int = 0,
    val totalAttemptsCount: Int = 0,
    val placementReadinessIndex: Int = 0,
    val solitarySolvedRatio: Int = 0,
    val topicStabilities: List<TopicStabilityMetrics> = emptyList(),
    val recentAttempts: List<RecallAttempt> = emptyList(),
    val isLoading: Boolean = true
)

class AnalyticsViewModel(
    private val getAnalyticsUseCase: GetAnalyticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAnalyticsUseCase().collect { domainData ->
                _uiState.value = AnalyticsUiState(
                    totalProblems = domainData.totalProblems,
                    totalAttemptsCount = domainData.totalAttemptsCount,
                    placementReadinessIndex = domainData.placementReadinessIndex,
                    solitarySolvedRatio = domainData.solitarySolvedRatio,
                    topicStabilities = domainData.topicStabilities,
                    recentAttempts = domainData.recentAttempts,
                    isLoading = false
                )
            }
        }
    }
}
