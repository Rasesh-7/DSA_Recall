package com.example.dsarecall.domain.usecase

import com.example.dsarecall.domain.model.Difficulty
import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.RecallAttempt
import com.example.dsarecall.domain.model.SolutionReliance
import com.example.dsarecall.domain.model.SourceSheet
import com.example.dsarecall.domain.model.TopicTag
import com.example.dsarecall.domain.repository.ProblemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class FakeProblemRepository : ProblemRepository {
    private val problems = mutableListOf<Problem>()
    private val attempts = mutableListOf<RecallAttempt>()

    override fun observeAllProblems(): Flow<List<Problem>> = flowOf(problems.toList())

    override fun observeDueProblems(): Flow<List<Problem>> {
        val now = System.currentTimeMillis()
        return flowOf(problems.filter { it.isTracking && it.nextDueDate <= now })
    }

    override fun observeProblemsByTopic(topicTag: TopicTag): Flow<List<Problem>> {
        return flowOf(problems.filter { it.topicTags.contains(topicTag) })
    }

    override fun observeProblemsBySheet(sourceSheet: SourceSheet): Flow<List<Problem>> {
        return flowOf(problems.toList())
    }

    override suspend fun getProblemById(id: String): Problem? {
        return problems.firstOrNull { it.id == id }
    }

    override suspend fun setProblemTracking(id: String, isTracking: Boolean) {
        val index = problems.indexOfFirst { it.id == id }
        if (index != -1) {
            problems[index] = problems[index].copy(isTracking = isTracking)
        }
    }

    override suspend fun bulkUpdateTracking(problemIds: List<String>, isTracking: Boolean, initialDueDate: Long) {
        problemIds.forEach { pid ->
            val index = problems.indexOfFirst { it.id == pid }
            if (index != -1) {
                problems[index] = problems[index].copy(isTracking = isTracking, nextDueDate = initialDueDate)
            }
        }
    }

    override suspend fun ensureStarterProblemsTracked(targetSheet: SourceSheet, count: Int) {
        val now = System.currentTimeMillis()
        problems.take(count).forEachIndexed { i, p ->
            problems[i] = p.copy(isTracking = true, nextDueDate = now)
        }
    }

    override suspend fun activateMoreStarterProblems(targetSheet: SourceSheet, count: Int) {
        val now = System.currentTimeMillis()
        val untracked = problems.filter { !it.isTracking }
        untracked.take(count).forEach { p ->
            val index = problems.indexOfFirst { it.id == p.id }
            if (index != -1) {
                problems[index] = p.copy(isTracking = true, nextDueDate = now)
            }
        }
    }

    override suspend fun insertProblem(problem: Problem) {
        problems.removeAll { it.id == problem.id }
        problems.add(problem)
    }

    override suspend fun insertProblems(newProblems: List<Problem>) {
        newProblems.forEach { insertProblem(it) }
    }

    override suspend fun updateProblem(problem: Problem) {
        insertProblem(problem)
    }

    override suspend fun deleteProblem(id: String) {
        problems.removeAll { it.id == id }
    }

    override suspend fun recordRecallAttempt(problemId: String, attempt: RecallAttempt): Problem {
        val problem = getProblemById(problemId) ?: throw IllegalArgumentException("Problem not found")
        attempts.add(attempt)
        val updated = problem.copy(
            isTracking = true,
            lastReviewedAt = attempt.timestamp,
            totalAttempts = problem.totalAttempts + 1,
            successfulAttempts = if (attempt.recallScore >= 3) problem.successfulAttempts + 1 else problem.successfulAttempts
        )
        updateProblem(updated)
        return updated
    }

    override fun observeAttemptsForProblem(problemId: String): Flow<List<RecallAttempt>> {
        return flowOf(attempts.filter { it.problemId == problemId })
    }

    override fun observeAllAttempts(): Flow<List<RecallAttempt>> = flowOf(attempts.toList())
}

class UseCaseTest {

    private lateinit var fakeRepository: FakeProblemRepository
    private lateinit var getDailyQueueUseCase: GetDailyQueueUseCase
    private lateinit var logRecallAttemptUseCase: LogRecallAttemptUseCase
    private lateinit var getProblemBankUseCase: GetProblemBankUseCase

    @Before
    fun setUp() = runBlocking {
        fakeRepository = FakeProblemRepository()
        getDailyQueueUseCase = GetDailyQueueUseCase(fakeRepository)
        logRecallAttemptUseCase = LogRecallAttemptUseCase(fakeRepository)
        getProblemBankUseCase = GetProblemBankUseCase(fakeRepository)

        val now = System.currentTimeMillis()
        val dueProblem1 = Problem(
            id = "nc-3",
            title = "Two Sum",
            difficulty = Difficulty.EASY,
            topicTags = listOf(TopicTag.ARRAYS),
            isTracking = true,
            nextDueDate = now - 1000L
        )

        val dueProblem2 = Problem(
            id = "st-9",
            title = "Kadane's Algorithm Maximum Subarray Sum",
            difficulty = Difficulty.MEDIUM,
            topicTags = listOf(TopicTag.DYNAMIC_PROGRAMMING),
            isTracking = true,
            nextDueDate = now - 2000L
        )

        fakeRepository.insertProblems(listOf(dueProblem1, dueProblem2))
    }

    @Test
    fun `GetDailyQueueUseCase returns all due checklist problems when filter is null`() = runBlocking {
        val dueProblems = getDailyQueueUseCase().first()
        assertEquals(2, dueProblems.size)
    }

    @Test
    fun `GetDailyQueueUseCase filters due problems by topic tag`() = runBlocking {
        val dpProblems = getDailyQueueUseCase(TopicTag.DYNAMIC_PROGRAMMING).first()
        assertEquals(1, dpProblems.size)
        assertEquals("Kadane's Algorithm Maximum Subarray Sum", dpProblems.first().title)
    }

    @Test
    fun `GetProblemBankUseCase filters by query accurately`() = runBlocking {
        val searchResult = getProblemBankUseCase(query = "Kadane").first()
        assertEquals(1, searchResult.size)
        assertEquals("Kadane's Algorithm Maximum Subarray Sum", searchResult.first().title)
    }

    @Test
    fun `LogRecallAttemptUseCase records attempt and updates problem in repository`() = runBlocking {
        val attempt = RecallAttempt(
            id = "att-1",
            problemId = "nc-3",
            recallScore = 5,
            solutionReliance = SolutionReliance.SOLVED_SOLITARY
        )

        val updatedProblem = logRecallAttemptUseCase("nc-3", attempt)
        assertNotNull(updatedProblem)
        assertEquals(1, updatedProblem.totalAttempts)
        assertEquals(1, updatedProblem.successfulAttempts)
    }
}
