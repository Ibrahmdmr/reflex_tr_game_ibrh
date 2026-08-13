package com.reflex.tr.game.ibrh.ui.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the game economy (coins / XP / level) and daily challenge progression.
 * Breaking these calculations hands out wrong rewards, so this is the most
 * critical pure logic in the app.
 */
class GameProgressionCalculatorsTest {

    // --- calculateEarnedCoins ---

    @Test
    fun `awards no coins for a scoreless game`() {
        assertEquals(0, calculateEarnedCoins(score = 0, maxCombo = 0, isNewBestScore = false))
    }

    @Test
    fun `awards five coins per point of score`() {
        assertEquals(50, calculateEarnedCoins(score = 10, maxCombo = 0, isNewBestScore = false))
    }

    @Test
    fun `awards a tiered bonus per combo threshold`() {
        // Measured at score 10 (= 50 coins) to stay clear of the 10 coin floor.
        val base = 50
        assertEquals(base + 0, calculateEarnedCoins(score = 10, maxCombo = 1, isNewBestScore = false))
        assertEquals(base + 15, calculateEarnedCoins(score = 10, maxCombo = 2, isNewBestScore = false))
        assertEquals(base + 35, calculateEarnedCoins(score = 10, maxCombo = 5, isNewBestScore = false))
        assertEquals(base + 70, calculateEarnedCoins(score = 10, maxCombo = 10, isNewBestScore = false))
        assertEquals(base + 120, calculateEarnedCoins(score = 10, maxCombo = 20, isNewBestScore = false))
    }

    @Test
    fun `awards eighty bonus coins for a new best score`() {
        val without = calculateEarnedCoins(score = 4, maxCombo = 0, isNewBestScore = false)
        val with = calculateEarnedCoins(score = 4, maxCombo = 0, isNewBestScore = true)
        assertEquals(80, with - without)
    }

    @Test
    fun `guarantees a floor of ten coins once the player scores`() {
        // Score 1 earns 5 coins, which is raised to the 10 coin floor.
        assertEquals(10, calculateEarnedCoins(score = 1, maxCombo = 0, isNewBestScore = false))
    }

    // --- calculateEarnedXp ---

    @Test
    fun `awards a base of twenty xp`() {
        assertEquals(20, calculateEarnedXp(score = 0, hits = 0, maxCombo = 0, isNewBestScore = false))
    }

    @Test
    fun `sums the score hit and combo xp components`() {
        // 20 base + (5 * 3) score + 7 hits + 25 combo (>= 5) + 60 new best score
        assertEquals(
            127,
            calculateEarnedXp(score = 5, hits = 7, maxCombo = 5, isNewBestScore = true)
        )
    }

    @Test
    fun `awards more xp once the combo reaches ten`() {
        val combo5 = calculateEarnedXp(score = 0, hits = 0, maxCombo = 5, isNewBestScore = false)
        val combo10 = calculateEarnedXp(score = 0, hits = 0, maxCombo = 10, isNewBestScore = false)
        assertEquals(20, combo10 - combo5)
    }

    // --- calculateProgressionLevel ---

    @Test
    fun `gains one level per two hundred fifty xp`() {
        assertEquals(1, calculateProgressionLevel(0))
        assertEquals(1, calculateProgressionLevel(249))
        assertEquals(2, calculateProgressionLevel(250))
        assertEquals(5, calculateProgressionLevel(1_000))
    }

    @Test
    fun `never drops below level one for negative xp`() {
        assertEquals(1, calculateProgressionLevel(-500))
    }

    // --- advanceDailyChallengeForHit ---

    @Test
    fun `advances a challenge only in its own mode`() {
        val state = DailyChallengeState.default() // ClassicScore20, target 20

        val wrongMode = advanceDailyChallengeForHit(
            state = state,
            mode = GameMode.MovingTarget,
            score = 7,
            combo = 0
        )
        assertEquals(0, wrongMode.progress)

        val rightMode = advanceDailyChallengeForHit(
            state = state,
            mode = GameMode.Classic,
            score = 7,
            combo = 0
        )
        assertEquals(7, rightMode.progress)
    }

    @Test
    fun `caps progress at the target and marks it completed`() {
        val state = DailyChallengeState.default()

        val result = advanceDailyChallengeForHit(
            state = state,
            mode = GameMode.Classic,
            score = 999,
            combo = 0
        )
        assertEquals(state.target, result.progress)
        assertTrue(result.completed)
    }

    @Test
    fun `leaves an already completed challenge untouched`() {
        val completed = DailyChallengeState.default().copy(progress = 20, completed = true)

        val result = advanceDailyChallengeForHit(
            state = completed,
            mode = GameMode.Classic,
            score = 999,
            combo = 99
        )
        assertEquals(completed, result)
    }

    @Test
    fun `advances a combo challenge regardless of mode`() {
        val state = DailyChallengeState.default().copy(
            type = DailyChallenge.Combo5,
            target = DailyChallenge.Combo5.targetValue,
            progress = 0,
            completed = false
        )

        val result = advanceDailyChallengeForHit(
            state = state,
            mode = GameMode.FakeTarget,
            score = 0,
            combo = 3
        )
        assertEquals(3, result.progress)
        assertFalse(result.completed)
    }

    // --- advanceDailyChallengeForGameCompleted ---

    @Test
    fun `advances the play games challenge once per finished game`() {
        val state = DailyChallengeState.default().copy(
            type = DailyChallenge.Play3Games,
            target = 3,
            progress = 0,
            completed = false
        )

        val afterFirst = advanceDailyChallengeForGameCompleted(state)
        assertEquals(1, afterFirst.progress)
        assertFalse(afterFirst.completed)

        val afterThird = advanceDailyChallengeForGameCompleted(
            advanceDailyChallengeForGameCompleted(afterFirst)
        )
        assertEquals(3, afterThird.progress)
        assertTrue(afterThird.completed)
    }

    @Test
    fun `leaves other challenge types untouched when a game finishes`() {
        val state = DailyChallengeState.default() // ClassicScore20
        assertEquals(state, advanceDailyChallengeForGameCompleted(state))
    }

    // --- advanceOneMoreGameBonusAfterCompletedGame ---

    @Test
    fun `keeps the one more game bonus claimed once it is awarded`() {
        val state = OneMoreGameBonusState()

        val afterAward = advanceOneMoreGameBonusAfterCompletedGame(state, bonusAwarded = true)
        assertEquals(1, afterAward.gamesPlayedToday)
        assertTrue(afterAward.bonusClaimedToday)

        val afterPlain = advanceOneMoreGameBonusAfterCompletedGame(afterAward, bonusAwarded = false)
        assertEquals(2, afterPlain.gamesPlayedToday)
        assertTrue("A claimed bonus must not be revoked", afterPlain.bonusClaimedToday)
    }
}
