package com.example.dsarecall.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "problems",
    indices = [
        Index(value = ["nextDueDate"]),
        Index(value = ["difficulty"]),
        Index(value = ["isTracking"])
    ]
)
data class ProblemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val difficulty: String,
    val topicTagsJoined: String, // Pipe separated string e.g. "ARRAYS|TWO_POINTERS"
    val description: String,
    val examplesJoined: String, // ||| separated string
    val constraintsJoined: String, // ||| separated string
    val isTracking: Boolean = false,
    val intervalDays: Int,
    val repetitionCount: Int,
    val easeFactor: Double,
    val lastReviewedAt: Long?,
    val nextDueDate: Long,
    val latestTrickNote: String,
    val totalAttempts: Int,
    val successfulAttempts: Int
)
