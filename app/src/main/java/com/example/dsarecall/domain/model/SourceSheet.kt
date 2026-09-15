package com.example.dsarecall.domain.model

enum class SourceSheet(val displayName: String) {
    NEETCODE_150("NeetCode 150"),
    STRIVER_A2Z("Striver's A2Z Sheet");

    companion object {
        fun fromString(value: String?): SourceSheet? {
            if (value.isNullOrBlank()) return null
            val v = value.trim()
            return values().firstOrNull {
                it.name.equals(v, ignoreCase = true) ||
                it.displayName.equals(v, ignoreCase = true) ||
                v.replace(" ", "_").equals(it.name, ignoreCase = true) ||
                (v.contains("striver", ignoreCase = true) && it == STRIVER_A2Z) ||
                (v.contains("neetcode", ignoreCase = true) && it == NEETCODE_150)
            }
        }
    }
}
