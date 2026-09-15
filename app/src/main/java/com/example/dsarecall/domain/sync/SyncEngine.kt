package com.example.dsarecall.domain.sync

import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.RecallAttempt
import com.example.dsarecall.domain.model.SyncStatus
import com.example.dsarecall.domain.repository.CloudStorageRepository
import com.example.dsarecall.domain.repository.ProblemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class SyncEngine(
    private val problemRepository: ProblemRepository,
    private val cloudStorageRepository: CloudStorageRepository
) {
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    suspend fun performSync(userId: String): Result<Unit> {
        if (userId.isBlank()) {
            _syncStatus.value = SyncStatus.Idle
            return Result.success(Unit)
        }

        _syncStatus.value = SyncStatus.Syncing
        return try {
            val localProblems = problemRepository.observeAllProblems().first()
            val cloudProblemsResult = cloudStorageRepository.fetchUserProblems(userId)
            val cloudProblems = cloudProblemsResult.getOrDefault(emptyList())

            val cloudProblemsMap = cloudProblems.associateBy { it.id }
            val localProblemsMap = localProblems.associateBy { it.id }

            val mergedProblemsToPush = mutableListOf<Problem>()

            for (localProblem in localProblems) {
                val cloudProblem = cloudProblemsMap[localProblem.id]
                if (cloudProblem == null) {
                    if (localProblem.isTracking || localProblem.totalAttempts > 0) {
                        mergedProblemsToPush.add(localProblem)
                    }
                } else {
                    // Conflict Resolution: Last-Write-Wins based on lastReviewedAt timestamp
                    val localTimestamp = localProblem.lastReviewedAt ?: 0L
                    val cloudTimestamp = cloudProblem.lastReviewedAt ?: 0L

                    if (cloudTimestamp > localTimestamp) {
                        // Cloud copy is newer, update local database
                        problemRepository.updateProblem(cloudProblem)
                    } else if (localTimestamp > cloudTimestamp) {
                        // Local copy is newer, queue for cloud push
                        mergedProblemsToPush.add(localProblem)
                    } else {
                        // Equal timestamp; if cloud is tracking and local is not, merge tracking state
                        if (cloudProblem.isTracking && !localProblem.isTracking) {
                            problemRepository.updateProblem(cloudProblem)
                        } else if (localProblem.isTracking && !cloudProblem.isTracking) {
                            mergedProblemsToPush.add(localProblem)
                        }
                    }
                }
            }

            // Also check cloud problems that might not exist in local at all
            for (cloudProblem in cloudProblems) {
                if (!localProblemsMap.containsKey(cloudProblem.id)) {
                    problemRepository.insertProblem(cloudProblem)
                }
            }

            // Push merged/updated local state to Cloud Storage
            if (mergedProblemsToPush.isNotEmpty()) {
                cloudStorageRepository.pushUserProblems(userId, mergedProblemsToPush)
            }

            // Sync Recall Attempts
            val localAttempts = problemRepository.observeAllAttempts().first()
            val cloudAttemptsResult = cloudStorageRepository.fetchUserAttempts(userId)
            val cloudAttempts = cloudAttemptsResult.getOrDefault(emptyList())

            val cloudAttemptIds = cloudAttempts.map { it.id }.toSet()
            val localAttemptIds = localAttempts.map { it.id }.toSet()

            val attemptsToPush = localAttempts.filter { it.id !in cloudAttemptIds }
            if (attemptsToPush.isNotEmpty()) {
                cloudStorageRepository.pushUserAttempts(userId, attemptsToPush)
            }

            val timestamp = System.currentTimeMillis()
            _syncStatus.value = SyncStatus.Synced(timestamp)
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Sync failed"
            _syncStatus.value = SyncStatus.Error(errorMsg)
            Result.failure(e)
        }
    }
}
