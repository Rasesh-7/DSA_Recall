package com.example.dsarecall.domain.usecase

import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.RecallAttempt
import com.example.dsarecall.domain.repository.ProblemRepository

class LogRecallAttemptUseCase(
    private val repository: ProblemRepository
) {
    suspend operator fun invoke(problemId: String, attempt: RecallAttempt): Problem {
        return repository.recordRecallAttempt(problemId, attempt)
    }
}
