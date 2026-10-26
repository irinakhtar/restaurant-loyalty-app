package com.regulars.domain

import java.time.Duration
import java.time.Instant

object RewardRules {
    const val STAMPS_REQUIRED: Int = 8
    val STAMP_COOLDOWN: Duration = Duration.ofHours(1)

    fun stampCount(visits: List<Visit>, restaurantId: String): Int {
        val lastClaimAt = lastClaimAt(visits, restaurantId)
        return visits.count { visit ->
            visit.restaurantId == restaurantId &&
                visit.kind == VisitKind.STAMP &&
                (lastClaimAt == null || visit.visitedAt.isAfter(lastClaimAt))
        }
    }

    fun isRewardUnlocked(visits: List<Visit>, restaurantId: String): Boolean {
        return stampCount(visits, restaurantId) >= STAMPS_REQUIRED
    }

    fun addStamp(
        visits: List<Visit>,
        restaurantId: String,
        at: Instant,
        visitId: String,
    ): AddStampResult {
        if (isRewardUnlocked(visits, restaurantId)) {
            return AddStampResult.CardFull
        }
        val lastStampAt = visits
            .asSequence()
            .filter { it.restaurantId == restaurantId && it.kind == VisitKind.STAMP }
            .maxOfOrNull { it.visitedAt }
        if (lastStampAt != null) {
            val nextStampAt = lastStampAt.plus(STAMP_COOLDOWN)
            if (at.isBefore(nextStampAt)) {
                return AddStampResult.OnCooldown(nextStampAt)
            }
        }
        return AddStampResult.Added(
            visits + Visit(
                id = visitId,
                restaurantId = restaurantId,
                visitedAt = at,
                kind = VisitKind.STAMP,
            ),
        )
    }

    fun claimReward(
        visits: List<Visit>,
        restaurantId: String,
        at: Instant,
        visitId: String,
    ): ClaimRewardResult {
        if (!isRewardUnlocked(visits, restaurantId)) {
            return ClaimRewardResult.NotReady
        }
        return ClaimRewardResult.Claimed(
            visits + Visit(
                id = visitId,
                restaurantId = restaurantId,
                visitedAt = at,
                kind = VisitKind.REWARD_CLAIM,
            ),
        )
    }

    private fun lastClaimAt(visits: List<Visit>, restaurantId: String): Instant? {
        return visits
            .asSequence()
            .filter { it.restaurantId == restaurantId && it.kind == VisitKind.REWARD_CLAIM }
            .maxOfOrNull { it.visitedAt }
    }
}

sealed interface AddStampResult {
    data class Added(val visits: List<Visit>) : AddStampResult

    data class OnCooldown(val nextStampAt: Instant) : AddStampResult

    data object CardFull : AddStampResult
}

sealed interface ClaimRewardResult {
    data class Claimed(val visits: List<Visit>) : ClaimRewardResult

    data object NotReady : ClaimRewardResult
}
