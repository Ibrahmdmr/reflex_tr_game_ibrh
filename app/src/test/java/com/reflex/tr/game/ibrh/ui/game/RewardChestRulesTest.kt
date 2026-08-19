package com.reflex.tr.game.ibrh.ui.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The chest decides real payouts from a random draw, so the parts that must never drift — the
 * one-chest-per-run cap, the every-N-games cadence and the payout bounds — are pinned here.
 */
class RewardChestRulesTest {

    // --- rollRewardChestType ---

    @Test
    fun `chest odds add up to a full hundred percent`() {
        assertEquals(100, RewardChestType.entries.sumOf { it.rollWeightPercent })
    }

    @Test
    fun `always draws a real tier, and the weakest one most often`() {
        val random = Random(7)
        val draws = List(1_000) { rollRewardChestType(random) }
        assertTrue(draws.all { it in RewardChestType.entries })
        assertTrue(draws.count { it == RewardChestType.Small } > draws.count { it == RewardChestType.Legendary })
    }

    // --- rollRewardChestReward ---

    @Test
    fun `pays within the tier range and never off a coin step`() {
        val random = Random(11)
        RewardChestType.entries.forEach { type ->
            repeat(200) {
                val reward = rollRewardChestReward(type, random)
                assertTrue(reward.coins in type.minCoins..type.maxCoins)
                assertEquals(0, reward.coins % 5)
            }
        }
    }

    @Test
    fun `pays either the tier season xp or none of it`() {
        val random = Random(3)
        repeat(200) {
            val reward = rollRewardChestReward(RewardChestType.Neon, random)
            assertTrue(reward.seasonXp == 0 || reward.seasonXp == RewardChestType.Neon.seasonXpReward)
        }
        assertEquals(0, rollRewardChestReward(RewardChestType.Small, random).seasonXp)
    }

    // --- earnRewardChestAfterGame ---

    private fun playPlainGame(state: RewardChestState): RewardChestEarn =
        earnRewardChestAfterGame(
            state = state,
            dailyEventJustCompleted = false,
            weeklyGoalJustCompleted = false,
            isNewBestScore = false
        )

    @Test
    fun `pays a small chest on every fifth run and restarts the count`() {
        var state = RewardChestState()
        repeat(GAMES_PER_REWARD_CHEST - 1) {
            val earn = playPlainGame(state)
            assertNull(earn.earnedChest)
            state = earn.state
        }

        val fifth = playPlainGame(state)
        assertEquals(RewardChestType.Small, fifth.earnedChest)
        assertEquals(RewardChestSource.GameCount, fifth.source)
        assertEquals(0, fifth.state.gamesSinceLastChest)
        assertEquals(1, fifth.state.pendingCount)
    }

    @Test
    fun `pays at most one chest even when every source fires at once`() {
        val earn = earnRewardChestAfterGame(
            state = RewardChestState(gamesSinceLastChest = GAMES_PER_REWARD_CHEST - 1),
            dailyEventJustCompleted = true,
            weeklyGoalJustCompleted = true,
            isNewBestScore = true
        )
        assertEquals(1, earn.state.pendingCount)
        assertEquals(RewardChestSource.GameCount, earn.source)
    }

    @Test
    fun `a bonus chest does not delay the every-fifth-run chest`() {
        val earn = earnRewardChestAfterGame(
            state = RewardChestState(gamesSinceLastChest = 1),
            dailyEventJustCompleted = true,
            weeklyGoalJustCompleted = false,
            isNewBestScore = false
        )
        assertNotNull(earn.earnedChest)
        assertEquals(RewardChestSource.DailyEvent, earn.source)
        assertEquals(2, earn.state.gamesSinceLastChest)
    }

    @Test
    fun `stops handing out chests at the cap but keeps counting runs`() {
        val full = RewardChestState(
            pendingChests = List(MAX_PENDING_REWARD_CHESTS) { RewardChestType.Small },
            gamesSinceLastChest = GAMES_PER_REWARD_CHEST - 1
        )
        val earn = playPlainGame(full)
        assertNull(earn.earnedChest)
        assertNull(earn.source)
        assertEquals(MAX_PENDING_REWARD_CHESTS, earn.state.pendingCount)
        assertEquals(GAMES_PER_REWARD_CHEST, earn.state.gamesSinceLastChest)
    }

    // --- openBestRewardChest ---

    @Test
    fun `opening an empty stack does nothing`() {
        assertNull(openBestRewardChest(RewardChestState()))
    }

    @Test
    fun `opens the best chest first and removes exactly one`() {
        val state = RewardChestState(
            pendingChests = listOf(RewardChestType.Small, RewardChestType.Legendary, RewardChestType.Small)
        )
        val opened = openBestRewardChest(state, Random(5))
        assertNotNull(opened)
        assertEquals(RewardChestType.Legendary, opened?.reward?.type)
        assertEquals(listOf(RewardChestType.Small, RewardChestType.Small), opened?.state?.pendingChests)
        assertEquals(opened?.reward?.coins, opened?.state?.lastRewardCoins)
    }

    @Test
    fun `reports how many runs are left before the next chest`() {
        assertEquals(GAMES_PER_REWARD_CHEST, RewardChestState().gamesUntilNextChest)
        assertEquals(1, RewardChestState(gamesSinceLastChest = GAMES_PER_REWARD_CHEST - 1).gamesUntilNextChest)
        assertEquals(0, RewardChestState(gamesSinceLastChest = GAMES_PER_REWARD_CHEST).gamesUntilNextChest)
    }
}
