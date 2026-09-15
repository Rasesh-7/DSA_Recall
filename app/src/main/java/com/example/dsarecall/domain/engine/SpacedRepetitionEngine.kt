package com.example.dsarecall.domain.engine

import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.RecallAttempt
import com.example.dsarecall.domain.model.SolutionReliance
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Custom SuperMemo SM-2 Spaced Repetition Engine tailored for DSA problem memory decay.
 */
object SpacedRepetitionEngine {

    private const val MIN_EASE_FACTOR = 1.3
    private const val MILLIS_IN_DAY = 24 * 60 * 60 * 1000L

    data class SchedulingResult(
        val newIntervalDays: Int,
        val newRepetitionCount: Int,
        val newEaseFactor: Double,
        val nextDueDate: Long,
        val isSuccess: Boolean
    )

    /**
     * Calculates the next review date and updated SM-2 parameters for a given recall attempt.
     */
    fun calculateNextReview(
        problem: Problem,
        attempt: RecallAttempt,
        currentTimeMs: Long = System.currentTimeMillis()
    ): SchedulingResult {
        val effectiveQuality = calculateEffectiveQuality(attempt.recallScore, attempt.solutionReliance)
        val isSuccess = effectiveQuality >= 3

        var newEaseFactor = problem.easeFactor + (0.1 - (5 - effectiveQuality) * (0.08 + (5 - effectiveQuality) * 0.02))
        if (newEaseFactor < MIN_EASE_FACTOR) {
            newEaseFactor = MIN_EASE_FACTOR
        }

        val newRepetitionCount: Int
        val newIntervalDays: Int

        if (!isSuccess) {
            newRepetitionCount = 0
            newIntervalDays = 1
        } else {
            newRepetitionCount = problem.repetitionCount + 1
            newIntervalDays = when (effectiveQuality) {
                5 -> {
                    // Rating 5 (Easy / Solitary Instant Recall): 7-day initial leap, then 1.3x ease factor multiplier
                    if (newRepetitionCount == 1) 7 else max(7, (problem.intervalDays * newEaseFactor * 1.3).roundToInt())
                }
                4 -> {
                    // Rating 4 (Good / Solitary Standard): 3-day initial leap, then 1.0x ease factor multiplier
                    if (newRepetitionCount == 1) 3 else max(3, (problem.intervalDays * newEaseFactor).roundToInt())
                }
                3 -> {
                    // Rating 3 (Hard / Needed Hint): 1-day initial step, then gradual 1.2x scaling
                    if (newRepetitionCount == 1) 1 else max(1, (problem.intervalDays * 1.2).roundToInt())
                }
                else -> 1
            }
        }

        val nextDueDate = currentTimeMs + (newIntervalDays * MILLIS_IN_DAY)

        return SchedulingResult(
            newIntervalDays = newIntervalDays,
            newRepetitionCount = newRepetitionCount,
            newEaseFactor = newEaseFactor,
            nextDueDate = nextDueDate,
            isSuccess = isSuccess
        )
    }

    /**
     * Calculates effective quality score (0..5) combining user confidence score and hint reliance.
     */
    fun calculateEffectiveQuality(recallScore: Int, reliance: SolutionReliance): Int {
        val baseScore = recallScore.coerceIn(1, 5)
        val adjustedScore = when (reliance) {
            SolutionReliance.SOLVED_SOLITARY -> baseScore
            SolutionReliance.NEEDED_HINT -> baseScore - 1
            SolutionReliance.LOOKED_AT_SOLUTION -> baseScore - 2
            SolutionReliance.FAILED -> 1
        }
        return adjustedScore.coerceIn(1, 5)
    }
}
