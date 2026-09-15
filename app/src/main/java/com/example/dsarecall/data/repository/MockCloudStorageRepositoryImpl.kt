package com.example.dsarecall.data.repository

import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.RecallAttempt
import com.example.dsarecall.domain.repository.CloudStorageRepository

class MockCloudStorageRepositoryImpl : CloudStorageRepository {
    private val cloudProblemStorage = mutableMapOf<String, MutableMap<String, Problem>>()
    private val cloudAttemptStorage = mutableMapOf<String, MutableMap<String, RecallAttempt>>()

    override suspend fun pushUserProblems(userId: String, problems: List<Problem>): Result<Unit> {
        val userMap = cloudProblemStorage.getOrPut(userId) { mutableMapOf() }
        for (problem in problems) {
            userMap[problem.id] = problem
        }
        return Result.success(Unit)
    }

    override suspend fun fetchUserProblems(userId: String): Result<List<Problem>> {
        val userMap = cloudProblemStorage[userId] ?: emptyMap()
        return Result.success(userMap.values.toList())
    }

    override suspend fun pushUserAttempts(userId: String, attempts: List<RecallAttempt>): Result<Unit> {
        val userMap = cloudAttemptStorage.getOrPut(userId) { mutableMapOf() }
        for (attempt in attempts) {
            userMap[attempt.id] = attempt
        }
        return Result.success(userMap.values.toList().let { Unit })
    }

    override suspend fun fetchUserAttempts(userId: String): Result<List<RecallAttempt>> {
        val userMap = cloudAttemptStorage[userId] ?: emptyMap()
        return Result.success(userMap.values.toList())
    }

    override suspend fun clearCloudUserData(userId: String): Result<Unit> {
        cloudProblemStorage.remove(userId)
        cloudAttemptStorage.remove(userId)
        return Result.success(Unit)
    }
}
