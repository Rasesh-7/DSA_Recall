package com.example.dsarecall.domain.usecase

import com.example.dsarecall.domain.model.Difficulty
import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.SourceSheet
import com.example.dsarecall.domain.model.TopicTag
import com.example.dsarecall.domain.repository.ProblemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetProblemBankUseCase(
    private val repository: ProblemRepository
) {
    operator fun invoke(
        query: String = "",
        topic: TopicTag? = null,
        difficulty: Difficulty? = null,
        sourceSheet: SourceSheet? = null
    ): Flow<List<Problem>> {
        val baseFlow = if (sourceSheet != null) {
            repository.observeProblemsBySheet(sourceSheet)
        } else {
            repository.observeAllProblems()
        }
        return baseFlow.map { problems ->
            problems.filter { problem ->
                val matchesQuery = query.isBlank() || problem.title.contains(query, ignoreCase = true)
                val matchesTopic = topic == null || problem.topicTags.contains(topic)
                val matchesDifficulty = difficulty == null || problem.difficulty == difficulty
                matchesQuery && matchesTopic && matchesDifficulty
            }
        }
    }
}
