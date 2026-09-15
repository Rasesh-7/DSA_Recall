package com.example.dsarecall.data.repository

import android.content.Context
import android.content.SharedPreferences

class OnboardingRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dsa_recall_onboarding", Context.MODE_PRIVATE)

    fun hasCompletedOnboarding(): Boolean {
        // Baseline established; bypassing initial onboarding question screens
        return true
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_COMPLETED, completed).apply()
    }

    fun getTargetSheet(): String {
        return prefs.getString(KEY_TARGET_SHEET, "NEETCODE_150") ?: "NEETCODE_150"
    }

    fun getDailyQuota(): Int {
        return prefs.getInt(KEY_DAILY_QUOTA, 3)
    }

    fun saveDailyQuota(quota: Int) {
        prefs.edit().putInt(KEY_DAILY_QUOTA, quota).apply()
    }

    fun saveUserPreferences(targetSheet: String, goalTimeline: String, baselineScore: Int, dailyQuota: Int = 3) {
        prefs.edit()
            .putString(KEY_TARGET_SHEET, targetSheet)
            .putString(KEY_GOAL_TIMELINE, goalTimeline)
            .putInt(KEY_BASELINE_SCORE, baselineScore)
            .putInt(KEY_DAILY_QUOTA, dailyQuota)
            .apply()
    }

    companion object {
        private const val KEY_HAS_COMPLETED = "has_completed_onboarding"
        private const val KEY_TARGET_SHEET = "target_sheet"
        private const val KEY_GOAL_TIMELINE = "goal_timeline"
        private const val KEY_BASELINE_SCORE = "baseline_score"
        private const val KEY_DAILY_QUOTA = "daily_quota"
    }
}
