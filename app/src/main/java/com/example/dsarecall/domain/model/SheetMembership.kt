package com.example.dsarecall.domain.model

data class SheetMembership(
    val problemId: String,
    val sourceSheet: SourceSheet,
    val position: Int
)
