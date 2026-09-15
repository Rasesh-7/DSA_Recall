package com.example.dsarecall.data.repository

import com.example.dsarecall.data.local.dao.ProblemDao
import com.example.dsarecall.data.mapper.toDomain
import com.example.dsarecall.data.mapper.toEntity
import com.example.dsarecall.data.seed.PreloadedProblems
import com.example.dsarecall.domain.engine.SpacedRepetitionEngine
import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.RecallAttempt
import com.example.dsarecall.domain.model.SourceSheet
import com.example.dsarecall.domain.model.TopicTag
import com.example.dsarecall.domain.repository.ProblemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProblemRepositoryImpl(
    private val dao: ProblemDao
) : ProblemRepository {

    suspend fun seedInitialDataIfEmpty() {
        val seedProblems = PreloadedProblems.ALL_PRELOADED_PROBLEMS
        val seedMemberships = PreloadedProblems.ALL_SHEET_MEMBERSHIPS
        val existingCount = dao.getProblemCount()

        if (existingCount < seedProblems.size) {
            dao.insertProblems(seedProblems)
            dao.insertSheetMemberships(seedMemberships)
        }
        ensureStarterProblemsTracked(SourceSheet.NEETCODE_150, 3)
    }

    override fun observeAllProblems(): Flow<List<Problem>> {
        return dao.observeAllProblems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeDueProblems(): Flow<List<Problem>> {
        val now = System.currentTimeMillis()
        return dao.observeDueProblems(now).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeProblemsByTopic(topicTag: TopicTag): Flow<List<Problem>> {
        return dao.observeProblemsByTopic(topicTag.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeProblemsBySheet(sourceSheet: SourceSheet): Flow<List<Problem>> {
        return dao.observeProblemsBySheet(sourceSheet.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProblemById(id: String): Problem? {
        return dao.getProblemById(id)?.toDomain()
    }

    override suspend fun setProblemTracking(id: String, isTracking: Boolean) {
        if (isTracking) {
            dao.setBulkProblemTracking(listOf(id), true, System.currentTimeMillis())
        } else {
            dao.setProblemTracking(id, false)
        }
    }

    override suspend fun bulkUpdateTracking(problemIds: List<String>, isTracking: Boolean, initialDueDate: Long) {
        if (problemIds.isNotEmpty()) {
            dao.setBulkProblemTracking(problemIds, isTracking, initialDueDate)
        }
    }

    override suspend fun ensureStarterProblemsTracked(targetSheet: SourceSheet, count: Int) {
        val unattemptedTrackedCount = dao.getUnattemptedTrackedProblemCount()
        if (unattemptedTrackedCount > count) {
            dao.trimUnattemptedTrackedProblems(targetSheet.name, count)
        }
        val trackedCount = dao.getTrackedProblemCount()
        if (trackedCount < count) {
            val topIds = dao.getTopProblemIdsForSheet(targetSheet.name, count)
            if (topIds.isNotEmpty()) {
                dao.setBulkProblemTracking(topIds, isTracking = true, dueDate = System.currentTimeMillis())
            }
        }
    }

    override suspend fun activateMoreStarterProblems(targetSheet: SourceSheet, count: Int) {
        val untrackedIds = dao.getUntrackedProblemIdsForSheet(targetSheet.name, count)
        if (untrackedIds.isNotEmpty()) {
            dao.setBulkProblemTracking(untrackedIds, isTracking = true, dueDate = System.currentTimeMillis())
        }
    }

    override suspend fun insertProblem(problem: Problem) {
        dao.insertProblem(problem.toEntity())
    }

    override suspend fun insertProblems(problems: List<Problem>) {
        dao.insertProblems(problems.map { it.toEntity() })
    }

    override suspend fun updateProblem(problem: Problem) {
        dao.updateProblem(problem.toEntity())
    }

    override suspend fun deleteProblem(id: String) {
        dao.deleteProblem(id)
    }

    override suspend fun recordRecallAttempt(problemId: String, attempt: RecallAttempt): Problem {
        val problem = getProblemById(problemId) ?: throw IllegalArgumentException("Problem not found: $problemId")

        val schedulingResult = SpacedRepetitionEngine.calculateNextReview(problem, attempt)

        val updatedProblem = problem.copy(
            isTracking = true,
            intervalDays = schedulingResult.newIntervalDays,
            repetitionCount = schedulingResult.newRepetitionCount,
            easeFactor = schedulingResult.newEaseFactor,
            lastReviewedAt = attempt.timestamp,
            nextDueDate = schedulingResult.nextDueDate,
            latestTrickNote = if (attempt.keyIntuitionNote.isNotBlank()) attempt.keyIntuitionNote else problem.latestTrickNote,
            totalAttempts = problem.totalAttempts + 1,
            successfulAttempts = if (schedulingResult.isSuccess) problem.successfulAttempts + 1 else problem.successfulAttempts
        )

        val attemptWithCalculations = attempt.copy(
            calculatedIntervalDays = schedulingResult.newIntervalDays,
            calculatedEaseFactor = schedulingResult.newEaseFactor
        )

        dao.insertAttempt(attemptWithCalculations.toEntity())
        dao.updateProblem(updatedProblem.toEntity())

        return updatedProblem
    }

    override fun observeAttemptsForProblem(problemId: String): Flow<List<RecallAttempt>> {
        return dao.observeAttemptsForProblem(problemId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeAllAttempts(): Flow<List<RecallAttempt>> {
        return dao.observeAllAttempts().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
