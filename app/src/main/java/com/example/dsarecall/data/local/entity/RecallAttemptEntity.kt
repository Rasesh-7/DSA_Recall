package com.example.dsarecall.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recall_attempts",
    foreignKeys = [
        ForeignKey(
            entity = ProblemEntity::class,
            parentColumns = ["id"],
            childColumns = ["problemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["problemId"]),
        Index(value = ["timestamp"])
    ]
)
data class RecallAttemptEntity(
    @PrimaryKey val id: String,
    val problemId: String,
    val timestamp: Long,
    val recallScore: Int,
    val solutionReliance: String,
    val durationSeconds: Int,
    val keyIntuitionNote: String,
    val calculatedIntervalDays: Int,
    val calculatedEaseFactor: Double
)
