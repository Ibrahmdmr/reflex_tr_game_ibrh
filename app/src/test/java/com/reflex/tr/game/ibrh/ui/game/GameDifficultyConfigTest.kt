package com.reflex.tr.game.ibrh.ui.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Boundary behaviour of the difficulty curve: tier thresholds, lower-bound clamps
 * and the easing applied during a player's first five games.
 */
class GameDifficultyConfigTest {

    private val experiencedPlayer = ProgressionState(totalGames = FirstFiveExperienceGameLimit)
    private val newPlayer = ProgressionState(totalGames = 0)

    // --- tierForScore ---

    @Test
    fun `switches tier at the documented score boundaries`() {
        assertEquals(GameDifficultyTier.Easy, GameDifficultyConfig.tierForScore(0))
        assertEquals(GameDifficultyTier.Easy, GameDifficultyConfig.tierForScore(10))
        assertEquals(GameDifficultyTier.Medium, GameDifficultyConfig.tierForScore(11))
        assertEquals(GameDifficultyTier.Medium, GameDifficultyConfig.tierForScore(25))
        assertEquals(GameDifficultyTier.Hard, GameDifficultyConfig.tierForScore(26))
        assertEquals(GameDifficultyTier.Hard, GameDifficultyConfig.tierForScore(50))
        assertEquals(GameDifficultyTier.Extreme, GameDifficultyConfig.tierForScore(51))
    }

    @Test
    fun `treats a negative score as the easy tier`() {
        assertEquals(GameDifficultyTier.Easy, GameDifficultyConfig.tierForScore(-10))
    }

    @Test
    fun `maps the difficulty level onto the tier`() {
        assertEquals(1, calculateDifficultyLevel(0))
        assertEquals(2, calculateDifficultyLevel(11))
        assertEquals(3, calculateDifficultyLevel(26))
        assertEquals(4, calculateDifficultyLevel(51))
    }

    // --- targetSizeDp ---

    @Test
    fun `shrinks the target with score but never below the minimum`() {
        val atStart = GameDifficultyConfig.targetSizeDp(0, GameMode.Classic, experiencedPlayer)
        val midGame = GameDifficultyConfig.targetSizeDp(30, GameMode.Classic, experiencedPlayer)
        val extreme = GameDifficultyConfig.targetSizeDp(10_000, GameMode.Classic, experiencedPlayer)

        assertTrue("Target should shrink as the score grows", midGame < atStart)
        assertEquals("Should clamp at the minimum size", 50, extreme)
    }

    @Test
    fun `hands a larger target to a player in their first five games`() {
        val forNewPlayer = GameDifficultyConfig.targetSizeDp(10, GameMode.Classic, newPlayer)
        val forExperienced = GameDifficultyConfig.targetSizeDp(10, GameMode.Classic, experiencedPlayer)

        assertTrue("New players should get an easier target", forNewPlayer > forExperienced)
    }

    // --- visibleDurationMillis ---

    @Test
    fun `shortens the visible duration with score but never below the minimum`() {
        val atStart = GameDifficultyConfig.visibleDurationMillis(0, GameMode.Classic, experiencedPlayer)
        val extreme = GameDifficultyConfig.visibleDurationMillis(10_000, GameMode.Classic, experiencedPlayer)

        assertTrue(extreme < atStart)
        assertEquals(900L, extreme)
    }

    @Test
    fun `keeps the target visible longer during the first five games`() {
        val forNewPlayer = GameDifficultyConfig.visibleDurationMillis(5, GameMode.Classic, newPlayer)
        val forExperienced = GameDifficultyConfig.visibleDurationMillis(5, GameMode.Classic, experiencedPlayer)

        assertTrue(forNewPlayer > forExperienced)
    }

    // --- movementIntervalMillis ---

    @Test
    fun `clamps the movement interval at its minimum`() {
        val extreme = GameDifficultyConfig.movementIntervalMillis(
            score = 10_000,
            mode = GameMode.MovingTarget,
            progression = experiencedPlayer
        )
        assertEquals(330L, extreme)
    }

    // --- fakeTargetCount / wrongColorCount ---

    @Test
    fun `spawns fake targets only in the fake target mode`() {
        assertEquals(
            0,
            GameDifficultyConfig.fakeTargetCount(30, GameMode.Classic, experiencedPlayer)
        )
        assertEquals(
            2,
            GameDifficultyConfig.fakeTargetCount(30, GameMode.FakeTarget, experiencedPlayer)
        )
    }

    @Test
    fun `limits fake targets to one during the first five games`() {
        assertEquals(
            1,
            GameDifficultyConfig.fakeTargetCount(30, GameMode.FakeTarget, newPlayer)
        )
    }

    @Test
    fun `adds more wrong colors on higher tiers`() {
        val easy = GameDifficultyConfig.wrongColorCount(0, GameMode.ColorReflex, experiencedPlayer)
        val hard = GameDifficultyConfig.wrongColorCount(30, GameMode.ColorReflex, experiencedPlayer)

        assertEquals(2, easy)
        assertEquals(3, hard)
    }

    @Test
    fun `rotates the color rule more slowly during the first five games`() {
        val forNewPlayer = GameDifficultyConfig.colorRuleIntervalMillis(0, GameMode.ColorReflex, newPlayer)
        val forExperienced = GameDifficultyConfig.colorRuleIntervalMillis(0, GameMode.ColorReflex, experiencedPlayer)

        assertEquals(500L, forNewPlayer - forExperienced)
    }

    @Test
    fun `defines a starting duration for every mode`() {
        GameMode.entries.forEach { mode ->
            assertTrue(
                "$mode should start with at least 10 seconds",
                GameDifficultyConfig.initialTimeSeconds(mode) >= 10
            )
        }
    }
}
