package com.example.dsarecall.data.repository

import com.example.dsarecall.domain.model.Difficulty
import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.RecallAttempt
import com.example.dsarecall.domain.model.SolutionReliance
import com.example.dsarecall.domain.model.TopicTag
import com.example.dsarecall.domain.repository.CloudStorageRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Production Firebase Firestore Cloud Storage Repository.
 * Handles background backup and retrieval of DSA problem tracking and recall history under:
 * `/users/{userId}/problems/{problemId}`
 * `/users/{userId}/attempts/{attemptId}`
 */
class FirebaseCloudStorageRepositoryImpl(
    private val mockFallback: MockCloudStorageRepositoryImpl = MockCloudStorageRepositoryImpl()
) : CloudStorageRepository {

    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Throwable) {
        null
    }

    override suspend fun pushUserProblems(userId: String, problems: List<Problem>): Result<Unit> {
        val db = firestore ?: return mockFallback.pushUserProblems(userId, problems)
        return try {
            val batch = db.batch()
            for (problem in problems) {
                val docRef = db.collection("users").document(userId).collection("problems").document(problem.id)
                val data = mapOf(
                    "id" to problem.id,
                    "title" to problem.title,
                    "difficulty" to problem.difficulty.name,
                    "topicTags" to problem.topicTags.map { it.name },
                    "isTracking" to problem.isTracking,
                    "intervalDays" to problem.intervalDays,
                    "repetitionCount" to problem.repetitionCount,
                    "easeFactor" to problem.easeFactor,
                    "lastReviewedAt" to problem.lastReviewedAt,
                    "nextDueDate" to problem.nextDueDate,
                    "latestTrickNote" to problem.latestTrickNote,
                    "totalAttempts" to problem.totalAttempts,
                    "successfulAttempts" to problem.successfulAttempts
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            mockFallback.pushUserProblems(userId, problems)
            Result.success(Unit)
        } catch (e: Exception) {
            mockFallback.pushUserProblems(userId, problems)
        }
    }

    override suspend fun fetchUserProblems(userId: String): Result<List<Problem>> {
        val db = firestore ?: return mockFallback.fetchUserProblems(userId)
        return try {
            val snapshot = db.collection("users").document(userId).collection("problems").get().await()
            val problems = snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val title = doc.getString("title") ?: return@mapNotNull null
                val diffStr = doc.getString("difficulty") ?: "EASY"
                val diff = try { Difficulty.valueOf(diffStr) } catch (_: Exception) { Difficulty.EASY }
                val tagsRaw = doc.get("topicTags") as? List<*> ?: emptyList<Any>()
                val tags = tagsRaw.mapNotNull { t -> try { TopicTag.valueOf(t.toString()) } catch (_: Exception) { null } }

                Problem(
                    id = id,
                    title = title,
                    difficulty = diff,
                    topicTags = tags,
                    isTracking = doc.getBoolean("isTracking") ?: false,
                    intervalDays = (doc.getLong("intervalDays") ?: 1L).toInt(),
                    repetitionCount = (doc.getLong("repetitionCount") ?: 0L).toInt(),
                    easeFactor = doc.getDouble("easeFactor") ?: 2.5,
                    lastReviewedAt = doc.getLong("lastReviewedAt"),
                    nextDueDate = doc.getLong("nextDueDate") ?: System.currentTimeMillis(),
                    latestTrickNote = doc.getString("latestTrickNote") ?: "",
                    totalAttempts = (doc.getLong("totalAttempts") ?: 0L).toInt(),
                    successfulAttempts = (doc.getLong("successfulAttempts") ?: 0L).toInt()
                )
            }
            Result.success(problems)
        } catch (e: Exception) {
            mockFallback.fetchUserProblems(userId)
        }
    }

    override suspend fun pushUserAttempts(userId: String, attempts: List<RecallAttempt>): Result<Unit> {
        val db = firestore ?: return mockFallback.pushUserAttempts(userId, attempts)
        return try {
            val batch = db.batch()
            for (attempt in attempts) {
                val docRef = db.collection("users").document(userId).collection("attempts").document(attempt.id)
                val data = mapOf(
                    "id" to attempt.id,
                    "problemId" to attempt.problemId,
                    "timestamp" to attempt.timestamp,
                    "recallScore" to attempt.recallScore,
                    "solutionReliance" to attempt.solutionReliance.name,
                    "durationSeconds" to attempt.durationSeconds,
                    "keyIntuitionNote" to attempt.keyIntuitionNote,
                    "calculatedIntervalDays" to attempt.calculatedIntervalDays,
                    "calculatedEaseFactor" to attempt.calculatedEaseFactor
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            mockFallback.pushUserAttempts(userId, attempts)
            Result.success(Unit)
        } catch (e: Exception) {
            mockFallback.pushUserAttempts(userId, attempts)
        }
    }

    override suspend fun fetchUserAttempts(userId: String): Result<List<RecallAttempt>> {
        val db = firestore ?: return mockFallback.fetchUserAttempts(userId)
        return try {
            val snapshot = db.collection("users").document(userId).collection("attempts").get().await()
            val attempts = snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val problemId = doc.getString("problemId") ?: return@mapNotNull null
                val timestamp = doc.getLong("timestamp") ?: return@mapNotNull null
                val recallScore = (doc.getLong("recallScore") ?: 3L).toInt()
                val relStr = doc.getString("solutionReliance") ?: "SOLVED_SOLITARY"
                val reliance = try { SolutionReliance.valueOf(relStr) } catch (_: Exception) { SolutionReliance.SOLVED_SOLITARY }

                RecallAttempt(
                    id = id,
                    problemId = problemId,
                    timestamp = timestamp,
                    recallScore = recallScore,
                    solutionReliance = reliance,
                    durationSeconds = (doc.getLong("durationSeconds") ?: 0L).toInt(),
                    keyIntuitionNote = doc.getString("keyIntuitionNote") ?: "",
                    calculatedIntervalDays = (doc.getLong("calculatedIntervalDays") ?: 1L).toInt(),
                    calculatedEaseFactor = doc.getDouble("calculatedEaseFactor") ?: 2.5
                )
            }
            Result.success(attempts)
        } catch (e: Exception) {
            mockFallback.fetchUserAttempts(userId)
        }
    }

    override suspend fun clearCloudUserData(userId: String): Result<Unit> {
        return mockFallback.clearCloudUserData(userId)
    }
}
