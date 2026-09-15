package com.example.dsarecall.domain.engine

import com.example.dsarecall.domain.model.Difficulty
import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.RecallAttempt
import com.example.dsarecall.domain.model.SolutionReliance
import com.example.dsarecall.domain.model.TopicTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpacedRepetitionEngineTest {

    private val baseProblem = Problem(
        id = "1",
        title = "Two Sum",
        difficulty = Difficulty.EASY,
        topicTags = listOf(TopicTag.ARRAYS),
        intervalDays = 1,
        repetitionCount = 0,
        easeFactor = 2.5
    )

    @Test
    fun `first successful attempt with quality 5 sets interval to 7 days`() {
        val attempt = RecallAttempt(
            id = "att-1",
            problemId = "1",
            recallScore = 5,
            solutionReliance = SolutionReliance.SOLVED_SOLITARY
        )

        val result = SpacedRepetitionEngine.calculateNextReview(baseProblem, attempt, currentTimeMs = 1000L)

        assertTrue(result.isSuccess)
        assertEquals(1, result.newRepetitionCount)
        assertEquals(7, result.newIntervalDays)
        assertEquals(1000L + (7 * 24 * 60 * 60 * 1000L), result.nextDueDate)
    }

    @Test
    fun `first successful attempt with quality 4 sets interval to 3 days`() {
        val attempt = RecallAttempt(
            id = "att-2",
            problemId = "1",
            recallScore = 4,
            solutionReliance = SolutionReliance.SOLVED_SOLITARY
        )

        val result = SpacedRepetitionEngine.calculateNextReview(baseProblem, attempt, currentTimeMs = 1000L)

        assertTrue(result.isSuccess)
        assertEquals(1, result.newRepetitionCount)
        assertEquals(3, result.newIntervalDays)
    }

    @Test
    fun `second successful attempt with quality 5 scales interval further`() {
        val problemAfterRep1 = baseProblem.copy(repetitionCount = 1, intervalDays = 7, easeFactor = 2.5)
        val attempt = RecallAttempt(
            id = "att-3",
            problemId = "1",
            recallScore = 5,
            solutionReliance = SolutionReliance.SOLVED_SOLITARY
        )

        val result = SpacedRepetitionEngine.calculateNextReview(problemAfterRep1, attempt, currentTimeMs = 1000L)

        assertTrue(result.isSuccess)
        assertEquals(2, result.newRepetitionCount)
        assertEquals(24, result.newIntervalDays)
    }

    @Test
    fun `ease factor cannot drop below minimum floor 1 point 3`() {
        val lowEaseProblem = baseProblem.copy(easeFactor = 1.3)
        val hardAttempt = RecallAttempt(
            id = "att-low",
            problemId = "1",
            recallScore = 1,
            solutionReliance = SolutionReliance.FAILED
        )

        val result = SpacedRepetitionEngine.calculateNextReview(lowEaseProblem, hardAttempt, currentTimeMs = 1000L)

        assertEquals(1.3, result.newEaseFactor, 0.001)
    }

    @Test
    fun `failed attempt resets repetition count and interval to 1 day`() {
        val matureProblem = baseProblem.copy(repetitionCount = 5, intervalDays = 30, easeFactor = 2.5)
        val attempt = RecallAttempt(
            id = "att-fail",
            problemId = "1",
            recallScore = 1,
            solutionReliance = SolutionReliance.FAILED
        )

        val result = SpacedRepetitionEngine.calculateNextReview(matureProblem, attempt, currentTimeMs = 1000L)

        assertFalse(result.isSuccess)
        assertEquals(0, result.newRepetitionCount)
        assertEquals(1, result.newIntervalDays)
    }

    @Test
    fun `effective quality decreases when solution reliance requires hints`() {
        val solitaryQuality = SpacedRepetitionEngine.calculateEffectiveQuality(5, SolutionReliance.SOLVED_SOLITARY)
        val hintQuality = SpacedRepetitionEngine.calculateEffectiveQuality(5, SolutionReliance.NEEDED_HINT)
        val solutionQuality = SpacedRepetitionEngine.calculateEffectiveQuality(5, SolutionReliance.LOOKED_AT_SOLUTION)
        val failedQuality = SpacedRepetitionEngine.calculateEffectiveQuality(5, SolutionReliance.FAILED)

        assertEquals(5, solitaryQuality)
        assertEquals(4, hintQuality)
        assertEquals(3, solutionQuality)
        assertEquals(1, failedQuality)
    }
}
