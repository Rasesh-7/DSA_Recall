package com.example.dsarecall.domain.model

enum class SolutionReliance(val label: String) {
    SOLVED_SOLITARY("Solved Solitary 🧠"),
    NEEDED_HINT("Needed Hint 💡"),
    LOOKED_AT_SOLUTION("Looked at Solution 📖"),
    FAILED("Failed / Could Not Solve ❌")
}

data class RecallAttempt(
    val id: String,
    val problemId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val recallScore: Int, // 1 (Blackout) to 5 (Instant Optimal)
    val solutionReliance: SolutionReliance,
    val durationSeconds: Int = 0,
    val keyIntuitionNote: String = "",
    val calculatedIntervalDays: Int = 1,
    val calculatedEaseFactor: Double = 2.5
)
