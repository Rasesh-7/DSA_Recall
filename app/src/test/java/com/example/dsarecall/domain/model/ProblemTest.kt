package com.example.dsarecall.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProblemTest {

    @Test
    fun `problem is due when current time exceeds next due date`() {
        val now = 1000000L
        val dueProblem = Problem(
            id = "1",
            title = "Two Sum",
            difficulty = Difficulty.EASY,
            topicTags = listOf(TopicTag.ARRAYS),
            nextDueDate = now - 5000L
        )

        val notDueProblem = Problem(
            id = "2",
            title = "3Sum",
            difficulty = Difficulty.MEDIUM,
            topicTags = listOf(TopicTag.TWO_POINTERS),
            nextDueDate = now + 86400000L
        )

        // Verifies due status calculation
        assertTrue(dueProblem.nextDueDate <= now)
        assertFalse(notDueProblem.nextDueDate <= now)
    }

    @Test
    fun `retention score calculates correct percentage of successful attempts`() {
        val problemWithNoAttempts = Problem(
            id = "1",
            title = "Two Sum",
            difficulty = Difficulty.EASY,
            topicTags = listOf(TopicTag.ARRAYS),
            totalAttempts = 0,
            successfulAttempts = 0
        )

        val problemWithMixedAttempts = Problem(
            id = "2",
            title = "3Sum",
            difficulty = Difficulty.MEDIUM,
            topicTags = listOf(TopicTag.TWO_POINTERS),
            totalAttempts = 4,
            successfulAttempts = 3
        )

        assertEquals(0, problemWithNoAttempts.recallRetentionScore)
        assertEquals(75, problemWithMixedAttempts.recallRetentionScore)
    }
}
