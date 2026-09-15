package com.example.dsarecall.domain.model

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Synced(val lastSyncedAt: Long) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}
