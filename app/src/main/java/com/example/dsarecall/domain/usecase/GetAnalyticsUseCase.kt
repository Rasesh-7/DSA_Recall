package com.example.dsarecall.domain.usecase

import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.RecallAttempt
import com.example.dsarecall.domain.model.SolutionReliance
import com.example.dsarecall.domain.model.TopicTag
import com.example.dsarecall.domain.repository.ProblemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class TopicStabilityMetrics(
    val topicTag: TopicTag,
    val totalProblems: Int,
    val averageEaseFactor: Double,
    val stabilityPercentage: Int
)

data class DomainAnalyticsData(
    val totalProblems: Int = 0,
    val totalAttemptsCount: Int = 0,
    val placementReadinessIndex: Int = 0,
    val solitarySolvedRatio: Int = 0,
    val topicStabilities: List<TopicStabilityMetrics> = emptyList(),
    val recentAttempts: List<RecallAttempt> = emptyList()
)

class GetAnalyticsUseCase(
    private val repository: ProblemRepository
) {
    operator fun invoke(): Flow<DomainAnalyticsData> {
        return combine(
            repository.observeAllProblems(),
            repository.observeAllAttempts()
        ) { problems, attempts ->
            calculateAnalytics(problems, attempts)
        }
    }

    private fun calculateAnalytics(problems: List<Problem>, attempts: List<RecallAttempt>): DomainAnalyticsData {
        if (problems.isEmpty()) return DomainAnalyticsData()

        val totalProblems = problems.size
        val totalAttempts = attempts.size

        val solitaryCount = attempts.count { it.solutionReliance == SolutionReliance.SOLVED_SOLITARY }
        val solitaryRatio = if (totalAttempts > 0) ((solitaryCount.toDouble() / totalAttempts) * 100).toInt() else 0

        val avgEase = problems.map { it.easeFactor }.average()
        val dueCount = problems.count { it.isDueForRevision }
        val nonOverdueRatio = ((problems.size - dueCount).toDouble() / problems.size).coerceIn(0.0, 1.0)

        val easeScore = (((avgEase - 1.3) / 1.5) * 100).coerceIn(0.0, 100.0)
        val readinessIndex = ((easeScore * 0.6) + (nonOverdueRatio * 40)).toInt().coerceIn(0, 100)

        val topicStabilities = TopicTag.values().mapNotNull { tag ->
            val topicProblems = problems.filter { it.topicTags.contains(tag) }
            if (topicProblems.isEmpty()) null
            else {
                val topicAvgEase = topicProblems.map { it.easeFactor }.average()
                val topicDueCount = topicProblems.count { it.isDueForRevision }
                val stability = ((((topicAvgEase - 1.3) / 1.5) * 70) + (((topicProblems.size - topicDueCount).toDouble() / topicProblems.size) * 30)).toInt().coerceIn(0, 100)

                TopicStabilityMetrics(
                    topicTag = tag,
                    totalProblems = topicProblems.size,
                    averageEaseFactor = topicAvgEase,
                    stabilityPercentage = stability
                )
            }
        }.sortedBy { it.stabilityPercentage }

        return DomainAnalyticsData(
            totalProblems = totalProblems,
            totalAttemptsCount = totalAttempts,
            placementReadinessIndex = readinessIndex,
            solitarySolvedRatio = solitaryRatio,
            topicStabilities = topicStabilities,
            recentAttempts = attempts.take(10)
        )
    }
}
