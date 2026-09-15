package com.example.dsarecall.domain.sync

import com.example.dsarecall.data.repository.MockCloudStorageRepositoryImpl
import com.example.dsarecall.domain.model.Difficulty
import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.RecallAttempt
import com.example.dsarecall.domain.model.SolutionReliance
import com.example.dsarecall.domain.model.SyncStatus
import com.example.dsarecall.domain.model.TopicTag
import com.example.dsarecall.domain.repository.ProblemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SyncEngineTest {

    private class FakeProblemRepository : ProblemRepository {
        val problemsMap = mutableMapOf<String, Problem>()
        val attemptsList = mutableListOf<RecallAttempt>()
        private val _problemsFlow = MutableStateFlow<Map<String, Problem>>(emptyMap())

        fun setProblems(problems: List<Problem>) {
            problemsMap.clear()
            problems.forEach { problemsMap[it.id] = it }
            _problemsFlow.value = problemsMap.toMap()
        }

        override fun observeAllProblems(): Flow<List<Problem>> = _problemsFlow.map { it.values.toList() }
        override fun observeDueProblems(): Flow<List<Problem>> = _problemsFlow.map { it.values.filter { p -> p.isDueForRevision } }
        override fun observeProblemsByTopic(topicTag: TopicTag): Flow<List<Problem>> = _problemsFlow.map { it.values.filter { p -> topicTag in p.topicTags } }
        override fun observeProblemsBySheet(sourceSheet: com.example.dsarecall.domain.model.SourceSheet): Flow<List<Problem>> = observeAllProblems()

        override suspend fun getProblemById(id: String): Problem? = problemsMap[id]
        override suspend fun setProblemTracking(id: String, isTracking: Boolean) {
            problemsMap[id]?.let { problemsMap[id] = it.copy(isTracking = isTracking) }
            _problemsFlow.value = problemsMap.toMap()
        }

        override suspend fun bulkUpdateTracking(problemIds: List<String>, isTracking: Boolean, initialDueDate: Long) {
            problemIds.forEach { id ->
                problemsMap[id]?.let { problemsMap[id] = it.copy(isTracking = isTracking, nextDueDate = initialDueDate) }
            }
            _problemsFlow.value = problemsMap.toMap()
        }

        override suspend fun ensureStarterProblemsTracked(targetSheet: com.example.dsarecall.domain.model.SourceSheet, count: Int) {}
        override suspend fun activateMoreStarterProblems(targetSheet: com.example.dsarecall.domain.model.SourceSheet, count: Int) {}
        override suspend fun insertProblem(problem: Problem) {
            problemsMap[problem.id] = problem
            _problemsFlow.value = problemsMap.toMap()
        }
        override suspend fun insertProblems(problems: List<Problem>) {
            problems.forEach { problemsMap[it.id] = it }
            _problemsFlow.value = problemsMap.toMap()
        }
        override suspend fun updateProblem(problem: Problem) {
            problemsMap[problem.id] = problem
            _problemsFlow.value = problemsMap.toMap()
        }
        override suspend fun deleteProblem(id: String) {
            problemsMap.remove(id)
            _problemsFlow.value = problemsMap.toMap()
        }
        override suspend fun recordRecallAttempt(problemId: String, attempt: RecallAttempt): Problem {
            attemptsList.add(attempt)
            val existing = problemsMap[problemId] ?: Problem(id = problemId, title = "Problem", difficulty = Difficulty.EASY, topicTags = listOf(TopicTag.ARRAYS))
            val updated = existing.copy(lastReviewedAt = attempt.timestamp, totalAttempts = existing.totalAttempts + 1)
            problemsMap[problemId] = updated
            _problemsFlow.value = problemsMap.toMap()
            return updated
        }
        override fun observeAttemptsForProblem(problemId: String): Flow<List<RecallAttempt>> = MutableStateFlow(attemptsList.filter { it.problemId == problemId })
        override fun observeAllAttempts(): Flow<List<RecallAttempt>> = MutableStateFlow(attemptsList.toList())
    }

    private lateinit var fakeLocalRepo: FakeProblemRepository
    private lateinit var cloudRepo: MockCloudStorageRepositoryImpl
    private lateinit var syncEngine: SyncEngine

    private val sampleProblem = Problem(
        id = "two-sum",
        title = "Two Sum",
        difficulty = Difficulty.EASY,
        topicTags = listOf(TopicTag.ARRAYS),
        isTracking = true,
        lastReviewedAt = 1000L,
        repetitionCount = 1
    )

    @Before
    fun setUp() {
        fakeLocalRepo = FakeProblemRepository()
        cloudRepo = MockCloudStorageRepositoryImpl()
        syncEngine = SyncEngine(fakeLocalRepo, cloudRepo)
    }

    @Test
    fun `sync updates local state when cloud has newer lastReviewedAt timestamp`() = runBlocking {
        val olderLocal = sampleProblem.copy(lastReviewedAt = 1000L, repetitionCount = 1)
        val newerCloud = sampleProblem.copy(lastReviewedAt = 5000L, repetitionCount = 3)

        fakeLocalRepo.setProblems(listOf(olderLocal))
        cloudRepo.pushUserProblems("user-1", listOf(newerCloud))

        val result = syncEngine.performSync("user-1")

        assertTrue(result.isSuccess)
        val updatedLocal = fakeLocalRepo.getProblemById("two-sum")
        assertEquals(5000L, updatedLocal?.lastReviewedAt)
        assertEquals(3, updatedLocal?.repetitionCount)
        assertTrue(syncEngine.syncStatus.value is SyncStatus.Synced)
    }

    @Test
    fun `sync pushes local state when local has newer lastReviewedAt timestamp`() = runBlocking {
        val newerLocal = sampleProblem.copy(lastReviewedAt = 9000L, repetitionCount = 4)
        val olderCloud = sampleProblem.copy(lastReviewedAt = 2000L, repetitionCount = 2)

        fakeLocalRepo.setProblems(listOf(newerLocal))
        cloudRepo.pushUserProblems("user-1", listOf(olderCloud))

        val result = syncEngine.performSync("user-1")

        assertTrue(result.isSuccess)
        val cloudProblems = cloudRepo.fetchUserProblems("user-1").getOrNull()
        val syncedCloudProblem = cloudProblems?.find { it.id == "two-sum" }
        assertEquals(9000L, syncedCloudProblem?.lastReviewedAt)
        assertEquals(4, syncedCloudProblem?.repetitionCount)
    }

    @Test
    fun `sync pulls new cloud problem not present in local database`() = runBlocking {
        val newCloudProblem = Problem(
            id = "3sum",
            title = "3Sum",
            difficulty = Difficulty.MEDIUM,
            topicTags = listOf(TopicTag.TWO_POINTERS),
            isTracking = true,
            lastReviewedAt = 3000L
        )

        fakeLocalRepo.setProblems(emptyList())
        cloudRepo.pushUserProblems("user-1", listOf(newCloudProblem))

        val result = syncEngine.performSync("user-1")

        assertTrue(result.isSuccess)
        val pulledLocal = fakeLocalRepo.getProblemById("3sum")
        assertEquals("3Sum", pulledLocal?.title)
        assertTrue(pulledLocal?.isTracking == true)
    }
}
