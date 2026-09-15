package com.example.dsarecall.domain.model

data class Problem(
    val id: String,
    val title: String,
    val difficulty: Difficulty,
    val topicTags: List<TopicTag>,
    val description: String = "",
    val examples: List<String> = emptyList(),
    val constraints: List<String> = emptyList(),
    val isTracking: Boolean = false,
    val intervalDays: Int = 1,
    val repetitionCount: Int = 0,
    val easeFactor: Double = 2.5,
    val lastReviewedAt: Long? = null,
    val nextDueDate: Long = System.currentTimeMillis(),
    val latestTrickNote: String = "",
    val totalAttempts: Int = 0,
    val successfulAttempts: Int = 0
) {
    val isDueForRevision: Boolean
        get() = isTracking && System.currentTimeMillis() >= nextDueDate

    val recallRetentionScore: Int
        get() {
            if (totalAttempts == 0) return 0
            val successRatio = successfulAttempts.toDouble() / totalAttempts
            return (successRatio * 100).toInt()
        }
}
