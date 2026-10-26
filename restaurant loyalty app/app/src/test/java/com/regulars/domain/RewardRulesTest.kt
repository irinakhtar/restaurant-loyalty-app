package com.regulars.domain

import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RewardRulesTest {
    private val restaurantId = "corner-cup"
    private val otherRestaurantId = "lantern-bowl"
    private val noon = Instant.parse("2026-10-26T12:00:00Z")

    @Test
    fun eightStampsUnlockTheReward() {
        assertFalse(RewardRules.isRewardUnlocked(emptyList(), restaurantId))
        assertFalse(RewardRules.isRewardUnlocked(stamps(7), restaurantId))
        assertTrue(RewardRules.isRewardUnlocked(stamps(8), restaurantId))
        assertEquals(8, RewardRules.stampCount(stamps(8), restaurantId))
    }

    @Test
    fun claimingRecordsTheClaimAndResetsTheCardToZero() {
        val claimedAt = noon.plus(Duration.ofDays(2))
        val result =
            RewardRules.claimReward(
                visits = stamps(8),
                restaurantId = restaurantId,
                at = claimedAt,
                visitId = "claim-1",
            )

        val claimed = assertIs<ClaimRewardResult.Claimed>(result)
        val claim = claimed.visits.last()
        assertEquals("claim-1", claim.id)
        assertEquals(restaurantId, claim.restaurantId)
        assertEquals(claimedAt, claim.visitedAt)
        assertEquals(VisitKind.REWARD_CLAIM, claim.kind)
        assertEquals(9, claimed.visits.size)
        assertEquals(0, RewardRules.stampCount(claimed.visits, restaurantId))
        assertFalse(RewardRules.isRewardUnlocked(claimed.visits, restaurantId))
    }

    @Test
    fun claimingBeforeEightStampsLeavesTheCardUnchanged() {
        val visits = stamps(7)
        val result =
            RewardRules.claimReward(
                visits = visits,
                restaurantId = restaurantId,
                at = noon.plus(Duration.ofDays(2)),
                visitId = "claim-1",
            )

        assertIs<ClaimRewardResult.NotReady>(result)
        assertEquals(7, RewardRules.stampCount(visits, restaurantId))
    }

    @Test
    fun claimingOneRestaurantDoesNotResetAnotherCard() {
        val visits = stamps(8)
        val result =
            RewardRules.claimReward(
                visits = visits,
                restaurantId = otherRestaurantId,
                at = noon.plus(Duration.ofDays(2)),
                visitId = "claim-other",
            )

        assertIs<ClaimRewardResult.NotReady>(result)
        assertEquals(8, RewardRules.stampCount(visits, restaurantId))
    }

    @Test
    fun onlyOneStampPerRestaurantPerHour() {
        val first =
            assertIs<AddStampResult.Added>(
                RewardRules.addStamp(emptyList(), restaurantId, noon, "stamp-1"),
            )
        val tooSoon =
            RewardRules.addStamp(
                visits = first.visits,
                restaurantId = restaurantId,
                at = noon.plus(Duration.ofMinutes(59)),
                visitId = "stamp-2",
            )

        val cooldown = assertIs<AddStampResult.OnCooldown>(tooSoon)
        assertEquals(noon.plus(RewardRules.STAMP_COOLDOWN), cooldown.nextStampAt)
        assertEquals(1, RewardRules.stampCount(first.visits, restaurantId))
    }

    @Test
    fun aStampIsAllowedOnceTheHourHasPassed() {
        val first =
            assertIs<AddStampResult.Added>(
                RewardRules.addStamp(emptyList(), restaurantId, noon, "stamp-1"),
            )
        val second =
            RewardRules.addStamp(
                visits = first.visits,
                restaurantId = restaurantId,
                at = noon.plus(Duration.ofHours(1)),
                visitId = "stamp-2",
            )

        val added = assertIs<AddStampResult.Added>(second)
        assertEquals(2, RewardRules.stampCount(added.visits, restaurantId))
    }

    @Test
    fun anotherRestaurantCanBeStampedDuringTheCooldown() {
        val first =
            assertIs<AddStampResult.Added>(
                RewardRules.addStamp(emptyList(), restaurantId, noon, "stamp-1"),
            )
        val other =
            RewardRules.addStamp(
                visits = first.visits,
                restaurantId = otherRestaurantId,
                at = noon.plus(Duration.ofMinutes(1)),
                visitId = "stamp-other",
            )

        val added = assertIs<AddStampResult.Added>(other)
        assertEquals(1, RewardRules.stampCount(added.visits, restaurantId))
        assertEquals(1, RewardRules.stampCount(added.visits, otherRestaurantId))
    }

    @Test
    fun stampsBeforeAClaimDoNotCountTowardTheNextCard() {
        val claimedAt = noon.plus(Duration.ofDays(2))
        val claimed =
            assertIs<ClaimRewardResult.Claimed>(
                RewardRules.claimReward(
                    visits = stamps(8),
                    restaurantId = restaurantId,
                    at = claimedAt,
                    visitId = "claim-1",
                ),
            )
        val next =
            assertIs<AddStampResult.Added>(
                RewardRules.addStamp(
                    visits = claimed.visits,
                    restaurantId = restaurantId,
                    at = claimedAt.plus(Duration.ofHours(1)),
                    visitId = "stamp-next",
                ),
            )

        assertEquals(1, RewardRules.stampCount(next.visits, restaurantId))
        assertFalse(RewardRules.isRewardUnlocked(next.visits, restaurantId))
    }

    @Test
    fun claimingDoesNotBypassTheHourlyStampCooldown() {
        val lastStampAt = noon.plus(Duration.ofHours(7))
        val claimedAt = lastStampAt.plus(Duration.ofMinutes(10))
        val claimed =
            assertIs<ClaimRewardResult.Claimed>(
                RewardRules.claimReward(
                    visits = stamps(8),
                    restaurantId = restaurantId,
                    at = claimedAt,
                    visitId = "claim-1",
                ),
            )
        val tooSoon =
            RewardRules.addStamp(
                visits = claimed.visits,
                restaurantId = restaurantId,
                at = claimedAt.plus(Duration.ofMinutes(1)),
                visitId = "stamp-next",
            )

        assertIs<AddStampResult.OnCooldown>(tooSoon)
        assertEquals(0, RewardRules.stampCount(claimed.visits, restaurantId))
    }

    @Test
    fun aFullCardDoesNotTakeAnotherStamp() {
        val visits = stamps(8)
        val result =
            RewardRules.addStamp(
                visits = visits,
                restaurantId = restaurantId,
                at = noon.plus(Duration.ofDays(2)),
                visitId = "stamp-extra",
            )

        assertIs<AddStampResult.CardFull>(result)
        assertEquals(8, RewardRules.stampCount(visits, restaurantId))
    }

    private fun stamps(count: Int): List<Visit> =
        List(count) { index ->
            Visit(
                id = "stamp-$index",
                restaurantId = restaurantId,
                visitedAt = noon.plus(Duration.ofHours(index.toLong())),
                kind = VisitKind.STAMP,
            )
        }
}
