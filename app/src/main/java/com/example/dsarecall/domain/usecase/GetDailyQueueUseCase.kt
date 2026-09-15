package com.example.dsarecall.domain.usecase

import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.TopicTag
import com.example.dsarecall.domain.repository.ProblemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetDailyQueueUseCase(
    private val repository: ProblemRepository
) {
    operator fun invoke(topicFilter: TopicTag? = null): Flow<List<Problem>> {
        return repository.observeDueProblems().map { dueProblems ->
            if (topicFilter != null) {
                dueProblems.filter { problem -> problem.topicTags.contains(topicFilter) }
            } else {
                dueProblems
            }.sortedBy { it.nextDueDate } // Urgency sorting
        }
    }
}
