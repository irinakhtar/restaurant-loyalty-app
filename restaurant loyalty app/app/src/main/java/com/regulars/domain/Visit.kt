package com.regulars.domain

import java.time.Instant

data class Visit(
    val id: String,
    val restaurantId: String,
    val visitedAt: Instant,
    val kind: VisitKind,
)

enum class VisitKind {
    STAMP,
    REWARD_CLAIM,
}
