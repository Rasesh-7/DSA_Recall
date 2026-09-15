package com.example.dsarecall.domain.repository

import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.RecallAttempt
import com.example.dsarecall.domain.model.SourceSheet
import com.example.dsarecall.domain.model.TopicTag
import kotlinx.coroutines.flow.Flow

interface ProblemRepository {
    fun observeAllProblems(): Flow<List<Problem>>
    fun observeDueProblems(): Flow<List<Problem>>
    fun observeProblemsByTopic(topicTag: TopicTag): Flow<List<Problem>>
    fun observeProblemsBySheet(sourceSheet: SourceSheet): Flow<List<Problem>>
    suspend fun getProblemById(id: String): Problem?
    suspend fun setProblemTracking(id: String, isTracking: Boolean)
    suspend fun bulkUpdateTracking(problemIds: List<String>, isTracking: Boolean, initialDueDate: Long)
    suspend fun ensureStarterProblemsTracked(targetSheet: SourceSheet = SourceSheet.NEETCODE_150, count: Int = 15)
    suspend fun insertProblem(problem: Problem)
    suspend fun insertProblems(problems: List<Problem>)
    suspend fun updateProblem(problem: Problem)
    suspend fun deleteProblem(id: String)
    suspend fun recordRecallAttempt(problemId: String, attempt: RecallAttempt): Problem
    fun observeAttemptsForProblem(problemId: String): Flow<List<RecallAttempt>>
    fun observeAllAttempts(): Flow<List<RecallAttempt>>
}
