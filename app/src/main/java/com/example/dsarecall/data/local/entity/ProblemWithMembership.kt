package com.example.dsarecall.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ProblemWithMembership(
    @Embedded val problem: ProblemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "problemId"
    )
    val memberships: List<SheetMembershipEntity>
)
