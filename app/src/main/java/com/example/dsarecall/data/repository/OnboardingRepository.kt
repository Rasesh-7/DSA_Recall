package com.example.dsarecall.data.repository

import android.content.Context
import android.content.SharedPreferences

class OnboardingRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dsa_recall_onboarding", Context.MODE_PRIVATE)

    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_HAS_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_COMPLETED, completed).apply()
    }

    fun getTargetSheet(): String {
        return prefs.getString(KEY_TARGET_SHEET, "NEETCODE_150") ?: "NEETCODE_150"
    }

    fun saveUserPreferences(targetSheet: String, goalTimeline: String, baselineScore: Int) {
        prefs.edit()
            .putString(KEY_TARGET_SHEET, targetSheet)
            .putString(KEY_GOAL_TIMELINE, goalTimeline)
            .putInt(KEY_BASELINE_SCORE, baselineScore)
            .apply()
    }

    companion object {
        private const val KEY_HAS_COMPLETED = "has_completed_onboarding"
        private const val KEY_TARGET_SHEET = "target_sheet"
        private const val KEY_GOAL_TIMELINE = "goal_timeline"
        private const val KEY_BASELINE_SCORE = "baseline_score"
    }
}
