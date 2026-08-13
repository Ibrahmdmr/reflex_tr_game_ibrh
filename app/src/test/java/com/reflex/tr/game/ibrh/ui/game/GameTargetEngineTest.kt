package com.reflex.tr.game.ibrh.ui.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Target generation is randomised, so these tests assert invariants that must always
 * hold rather than exact values.
 */
class GameTargetEngineTest {

    private val engine = GameTargetEngine()
    private val experiencedPlayer = ProgressionState(totalGames = FirstFiveExperienceGameLimit)

    private fun targetsFor(mode: GameMode, score: Int = 30) = engine.generateTargets(
        mode = mode,
        score = score,
        progression = experiencedPlayer
    )

    // --- per-mode target composition ---

    @Test
    fun `generates exactly one correct target in every mode`() {
        GameMode.entries.forEach { mode ->
            val correctCount = targetsFor(mode).count { it.role == GameTargetRole.Correct }
            assertEquals("$mode should expose a single correct target", 1, correctCount)
        }
    }

    @Test
    fun `generates a single target for the classic and moving modes`() {
        assertEquals(1, targetsFor(GameMode.Classic).size)
        assertEquals(1, targetsFor(GameMode.MovingTarget).size)
    }

    @Test
    fun `adds fake targets according to the difficulty tier`() {
        val targets = targetsFor(GameMode.FakeTarget, score = 30)
        val fakeCount = targets.count { it.role == GameTargetRole.Fake }

        assertEquals(
            GameDifficultyConfig.fakeTargetCount(30, GameMode.FakeTarget, experiencedPlayer),
            fakeCount
        )
    }

    @Test
    fun `paints the correct target in the active color and decoys in other colors`() {
        val activeColor = ReflexTargetColor.Blue
        val targets = engine.generateTargets(
            mode = GameMode.ColorReflex,
            score = 30,
            activeColor = activeColor,
            progression = experiencedPlayer
        )

        val correct = targets.single { it.role == GameTargetRole.Correct }
        assertEquals(activeColor, correct.color)

        targets.filter { it.role == GameTargetRole.WrongColor }.forEach {
            assertNotEquals("A decoy must not use the active color", activeColor, it.color)
        }
    }

    @Test
    fun `assigns a unique id to every generated target`() {
        val allIds = (1..20).flatMap { targetsFor(GameMode.FakeTarget) }.map { it.id }
        assertEquals("Every target should get a unique id", allIds.size, allIds.toSet().size)
    }

    // --- position bounds ---

    @Test
    fun `keeps target positions inside the playable area`() {
        repeat(200) {
            val position = engine.generateRandomTargetPosition()
            assertTrue(
                "x=${position.xFraction} is out of bounds",
                position.xFraction in 0.15f..0.85f
            )
            assertTrue(
                "y=${position.yFraction} is out of bounds",
                position.yFraction in 0.2f..0.8f
            )
        }
    }

    @Test
    fun `firstCorrectPosition returns the position of the correct target`() {
        val targets = targetsFor(GameMode.ColorReflex)
        val correct = targets.single { it.role == GameTargetRole.Correct }

        assertEquals(correct.position, targets.firstCorrectPosition())
    }

    @Test
    fun `firstCorrectPosition falls back to the default position when empty`() {
        assertEquals(TargetPosition(), emptyList<GameTarget>().firstCorrectPosition())
    }

    // --- color rule rotation ---

    @Test
    fun `cycles the color rule and returns to the starting color`() {
        val start = ReflexTargetColor.Red
        val sequence = generateSequence(start) { engine.nextColorRule(it) }
            .take(5)
            .toList()

        assertEquals("Should wrap around after four steps", start, sequence.last())
        assertEquals(
            "All four colors in the cycle should be distinct",
            4,
            sequence.take(4).toSet().size
        )
    }

    @Test
    fun `randomTargetColor never returns the excluded color`() {
        repeat(100) {
            assertNotEquals(
                ReflexTargetColor.Gold,
                engine.randomTargetColor(except = ReflexTargetColor.Gold)
            )
        }
    }

    // --- movement interval ---

    @Test
    fun `shortens the movement interval as the score grows`() {
        val slow = engine.calculateMovementIntervalMillis(0, GameMode.MovingTarget, experiencedPlayer)
        val fast = engine.calculateMovementIntervalMillis(40, GameMode.MovingTarget, experiencedPlayer)

        assertTrue("Targets should move more often at higher scores", fast < slow)
    }
}
