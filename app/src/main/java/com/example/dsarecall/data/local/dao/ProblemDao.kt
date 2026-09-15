package com.example.dsarecall.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.dsarecall.data.local.entity.ProblemEntity
import com.example.dsarecall.data.local.entity.ProblemWithMembership
import com.example.dsarecall.data.local.entity.RecallAttemptEntity
import com.example.dsarecall.data.local.entity.SheetMembershipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProblemDao {

    @Query("SELECT * FROM problems ORDER BY title ASC")
    fun observeAllProblems(): Flow<List<ProblemEntity>>

    @Query("SELECT * FROM problems WHERE isTracking = 1 AND nextDueDate <= :currentTimeMs ORDER BY nextDueDate ASC")
    fun observeDueProblems(currentTimeMs: Long): Flow<List<ProblemEntity>>

    @Query("SELECT * FROM problems WHERE topicTagsJoined LIKE '%' || :tag || '%' ORDER BY title ASC")
    fun observeProblemsByTopic(tag: String): Flow<List<ProblemEntity>>

    @Transaction
    @Query("SELECT p.* FROM problems p INNER JOIN sheet_memberships sm ON p.id = sm.problemId WHERE sm.sourceSheet = :sheet ORDER BY sm.position ASC")
    fun observeProblemsBySheet(sheet: String): Flow<List<ProblemEntity>>

    @Transaction
    @Query("SELECT * FROM problems WHERE id = :id")
    fun observeProblemWithMembership(id: String): Flow<ProblemWithMembership?>

    @Query("SELECT * FROM sheet_memberships WHERE sourceSheet = :sheet ORDER BY position ASC")
    fun observeMembershipsForSheet(sheet: String): Flow<List<SheetMembershipEntity>>

    @Query("SELECT * FROM sheet_memberships WHERE sourceSheet = :sheet ORDER BY position ASC")
    suspend fun getMembershipsForSheet(sheet: String): List<SheetMembershipEntity>

    @Query("SELECT * FROM problems WHERE id = :id")
    suspend fun getProblemById(id: String): ProblemEntity?

    @Query("SELECT COUNT(*) FROM problems")
    suspend fun getProblemCount(): Int

    @Query("SELECT COUNT(*) FROM sheet_memberships WHERE sourceSheet = :sheet")
    suspend fun getSheetCount(sheet: String): Int

    @Query("SELECT COUNT(*) FROM problems WHERE isTracking = 1")
    suspend fun getTrackedProblemCount(): Int

    @Query("SELECT COUNT(*) FROM problems WHERE isTracking = 1 AND totalAttempts = 0")
    suspend fun getUnattemptedTrackedProblemCount(): Int

    @Query("UPDATE problems SET isTracking = 0 WHERE totalAttempts = 0 AND id NOT IN (SELECT p.id FROM problems p INNER JOIN sheet_memberships sm ON p.id = sm.problemId WHERE sm.sourceSheet = :sheet ORDER BY sm.position ASC LIMIT :count)")
    suspend fun trimUnattemptedTrackedProblems(sheet: String, count: Int)

    @Query("SELECT p.id FROM problems p INNER JOIN sheet_memberships sm ON p.id = sm.problemId WHERE sm.sourceSheet = :sheet ORDER BY sm.position ASC LIMIT :limit")
    suspend fun getTopProblemIdsForSheet(sheet: String, limit: Int): List<String>

    @Query("SELECT p.id FROM problems p INNER JOIN sheet_memberships sm ON p.id = sm.problemId WHERE sm.sourceSheet = :sheet AND p.isTracking = 0 ORDER BY sm.position ASC LIMIT :limit")
    suspend fun getUntrackedProblemIdsForSheet(sheet: String, limit: Int): List<String>

    @Query("UPDATE problems SET isTracking = :isTracking WHERE id = :id")
    suspend fun setProblemTracking(id: String, isTracking: Boolean)

    @Query("UPDATE problems SET isTracking = :isTracking, nextDueDate = :dueDate WHERE id IN (:ids)")
    suspend fun setBulkProblemTracking(ids: List<String>, isTracking: Boolean, dueDate: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblem(problem: ProblemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblems(problems: List<ProblemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSheetMemberships(memberships: List<SheetMembershipEntity>)

    @Update
    suspend fun updateProblem(problem: ProblemEntity)

    @Query("DELETE FROM problems WHERE id = :id")
    suspend fun deleteProblem(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: RecallAttemptEntity)

    @Query("SELECT * FROM recall_attempts WHERE problemId = :problemId ORDER BY timestamp DESC")
    fun observeAttemptsForProblem(problemId: String): Flow<List<RecallAttemptEntity>>

    @Query("SELECT * FROM recall_attempts ORDER BY timestamp DESC")
    fun observeAllAttempts(): Flow<List<RecallAttemptEntity>>
}
