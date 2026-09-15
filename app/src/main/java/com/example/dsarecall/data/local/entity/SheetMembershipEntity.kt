package com.example.dsarecall.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "sheet_memberships",
    primaryKeys = ["problemId", "sourceSheet"],
    foreignKeys = [
        ForeignKey(
            entity = ProblemEntity::class,
            parentColumns = ["id"],
            childColumns = ["problemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sourceSheet", "position"]),
        Index(value = ["problemId"])
    ]
)
data class SheetMembershipEntity(
    val problemId: String,
    val sourceSheet: String, // "NEETCODE_150" or "STRIVER_A2Z"
    val position: Int
)
