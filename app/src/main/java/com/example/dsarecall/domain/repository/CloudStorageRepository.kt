package com.example.dsarecall.domain.repository

import com.example.dsarecall.domain.model.Problem
import com.example.dsarecall.domain.model.RecallAttempt

interface CloudStorageRepository {
    suspend fun pushUserProblems(userId: String, problems: List<Problem>): Result<Unit>
    suspend fun fetchUserProblems(userId: String): Result<List<Problem>>
    
    suspend fun pushUserAttempts(userId: String, attempts: List<RecallAttempt>): Result<Unit>
    suspend fun fetchUserAttempts(userId: String): Result<List<RecallAttempt>>
    
    suspend fun clearCloudUserData(userId: String): Result<Unit>
}
