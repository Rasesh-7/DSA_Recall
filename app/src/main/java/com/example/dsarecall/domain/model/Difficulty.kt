package com.example.dsarecall.domain.model

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD;

    fun getDisplayName(): String = when (this) {
        EASY -> "Easy"
        MEDIUM -> "Medium"
        HARD -> "Hard"
    }
}
