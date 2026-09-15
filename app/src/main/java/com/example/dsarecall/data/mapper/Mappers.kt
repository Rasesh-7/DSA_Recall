package com.example.dsarecall.data.mapper

import com.example.dsarecall.data.local.entity.ProblemEntity
import com.example.dsarecall.data.local.entity.RecallAttemptEntity
import com.example.dsarecall.data.local.entity.SheetMembershipEntity
import com.example.dsarecall.domain.model.Difficulty
import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.RecallAttempt
import com.example.dsarecall.domain.model.SheetMembership
import com.example.dsarecall.domain.model.SolutionReliance
import com.example.dsarecall.domain.model.SourceSheet
import com.example.dsarecall.domain.model.TopicTag

fun ProblemEntity.toDomain(): Problem {
    val tags = if (topicTagsJoined.isBlank()) {
        emptyList()
    } else {
        topicTagsJoined.split("|").map { TopicTag.fromString(it) }
    }

    val examplesList = if (examplesJoined.isBlank()) {
        emptyList()
    } else {
        examplesJoined.split("|||")
    }

    val constraintsList = if (constraintsJoined.isBlank()) {
        emptyList()
    } else {
        constraintsJoined.split("|||")
    }

    return Problem(
        id = id,
        title = title,
        difficulty = runCatching { Difficulty.valueOf(difficulty) }.getOrDefault(Difficulty.EASY),
        topicTags = tags,
        description = description,
        examples = examplesList,
        constraints = constraintsList,
        isTracking = isTracking,
        intervalDays = intervalDays,
        repetitionCount = repetitionCount,
        easeFactor = easeFactor,
        lastReviewedAt = lastReviewedAt,
        nextDueDate = nextDueDate,
        latestTrickNote = latestTrickNote,
        totalAttempts = totalAttempts,
        successfulAttempts = successfulAttempts
    )
}

fun Problem.toEntity(): ProblemEntity {
    return ProblemEntity(
        id = id,
        title = title,
        difficulty = difficulty.name,
        topicTagsJoined = topicTags.joinToString("|") { it.name },
        description = description,
        examplesJoined = examples.joinToString("|||"),
        constraintsJoined = constraints.joinToString("|||"),
        isTracking = isTracking,
        intervalDays = intervalDays,
        repetitionCount = repetitionCount,
        easeFactor = easeFactor,
        lastReviewedAt = lastReviewedAt,
        nextDueDate = nextDueDate,
        latestTrickNote = latestTrickNote,
        totalAttempts = totalAttempts,
        successfulAttempts = successfulAttempts
    )
}

fun SheetMembershipEntity.toDomain(): SheetMembership {
    return SheetMembership(
        problemId = problemId,
        sourceSheet = SourceSheet.fromString(sourceSheet) ?: SourceSheet.NEETCODE_150,
        position = position
    )
}

fun SheetMembership.toEntity(): SheetMembershipEntity {
    return SheetMembershipEntity(
        problemId = problemId,
        sourceSheet = sourceSheet.name,
        position = position
    )
}

fun RecallAttemptEntity.toDomain(): RecallAttempt {
    return RecallAttempt(
        id = id,
        problemId = problemId,
        timestamp = timestamp,
        recallScore = recallScore,
        solutionReliance = runCatching { SolutionReliance.valueOf(solutionReliance) }.getOrDefault(SolutionReliance.SOLVED_SOLITARY),
        durationSeconds = durationSeconds,
        keyIntuitionNote = keyIntuitionNote,
        calculatedIntervalDays = calculatedIntervalDays,
        calculatedEaseFactor = calculatedEaseFactor
    )
}

fun RecallAttempt.toEntity(): RecallAttemptEntity {
    return RecallAttemptEntity(
        id = id,
        problemId = problemId,
        timestamp = timestamp,
        recallScore = recallScore,
        solutionReliance = solutionReliance.name,
        durationSeconds = durationSeconds,
        keyIntuitionNote = keyIntuitionNote,
        calculatedIntervalDays = calculatedIntervalDays,
        calculatedEaseFactor = calculatedEaseFactor
    )
}
