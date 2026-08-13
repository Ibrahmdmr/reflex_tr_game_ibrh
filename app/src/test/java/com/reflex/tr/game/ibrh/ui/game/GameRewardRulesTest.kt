package com.reflex.tr.game.ibrh.ui.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Covers the in-run bonus rules that used to be private to GameViewModel. These decide real
 * payouts and event triggers, so their boundaries are worth pinning down.
 */
class GameRewardRulesTest {

    // --- flawlessStreakBonusFor ---

    @Test
    fun `pays a bonus only on the exact streak milestones`() {
        assertEquals(0, flawlessStreakBonusFor(4))
        assertEquals(10, flawlessStreakBonusFor(5))
        assertEquals(0, flawlessStreakBonusFor(6))
        assertEquals(25, flawlessStreakBonusFor(10))
        assertEquals(75, flawlessStreakBonusFor(20))
        assertEquals(0, flawlessStreakBonusFor(25))
    }

    @Test
    fun `pays nothing for a zero or negative streak`() {
        assertEquals(0, flawlessStreakBonusFor(0))
        assertEquals(0, flawlessStreakBonusFor(-3))
    }

    // --- bossRoundThresholdForScore ---

    @Test
    fun `triggers the lowest boss round threshold the score has passed`() {
        assertNull(bossRoundThresholdForScore(newScore = 14, triggeredThresholds = emptySet()))
        assertEquals(15, bossRoundThresholdForScore(newScore = 15, triggeredThresholds = emptySet()))
        assertEquals(15, bossRoundThresholdForScore(newScore = 40, triggeredThresholds = emptySet()))
    }

    @Test
    fun `never re-triggers a boss round threshold that already fired`() {
        assertEquals(30, bossRoundThresholdForScore(newScore = 40, triggeredThresholds = setOf(15)))
        assertNull(bossRoundThresholdForScore(newScore = 40, triggeredThresholds = setOf(15, 30)))
    }

    // --- ultraMomentThresholdForCombo ---

    @Test
    fun `triggers the lowest ultra moment threshold the combo has passed`() {
        assertNull(ultraMomentThresholdForCombo(combo = 9, triggeredThresholds = emptySet()))
        assertEquals(10, ultraMomentThresholdForCombo(combo = 10, triggeredThresholds = emptySet()))
        assertEquals(20, ultraMomentThresholdForCombo(combo = 25, triggeredThresholds = setOf(10)))
    }

    // --- calculateBonusHourCoins ---

    @Test
    fun `pays no bonus hour coins while the bonus hour is inactive`() {
        assertEquals(0, calculateBonusHourCoins(100, BonusHourState(isActive = false)))
    }

    @Test
    fun `pays no bonus hour coins for a scoreless run`() {
        assertEquals(0, calculateBonusHourCoins(0, BonusHourState(isActive = true)))
    }

    @Test
    fun `pays the configured percentage during the bonus hour`() {
        val bonusHour = BonusHourState(isActive = true, coinBonusPercent = 25)
        assertEquals(25, calculateBonusHourCoins(100, bonusHour))
    }

    @Test
    fun `always pays at least one coin during the bonus hour`() {
        // 1 * 25 / 100 rounds down to 0, which the floor lifts back to 1.
        val bonusHour = BonusHourState(isActive = true, coinBonusPercent = 25)
        assertEquals(1, calculateBonusHourCoins(1, bonusHour))
    }

    // --- accuracyPercent ---

    @Test
    fun `reports zero accuracy before any attempt`() {
        assertEquals(0, accuracyPercent(hits = 0, attempts = 0))
    }

    @Test
    fun `reports accuracy as a whole percentage`() {
        assertEquals(50, accuracyPercent(hits = 1, attempts = 2))
        assertEquals(25, accuracyPercent(hits = 1, attempts = 4))
        assertEquals(100, accuracyPercent(hits = 4, attempts = 4))
    }

    @Test
    fun `clamps accuracy into the zero to hundred range`() {
        assertEquals(100, accuracyPercent(hits = 9, attempts = 4))
        assertEquals(0, accuracyPercent(hits = -5, attempts = 4))
    }

    // --- sanitizePlayerName ---

    @Test
    fun `trims and caps the player name at twelve characters`() {
        assertEquals("Ahmet", sanitizePlayerName("  Ahmet  "))
        assertEquals("A".repeat(12), sanitizePlayerName("A".repeat(20)))
    }

    @Test
    fun `rejects a blank player name`() {
        assertNull(sanitizePlayerName("   "))
    }

    @Test
    fun `rejects a player name containing a blocked term`() {
        assertNull(sanitizePlayerName("shit"))
        assertNull(sanitizePlayerName("xxFUCKxx"))
    }
}
