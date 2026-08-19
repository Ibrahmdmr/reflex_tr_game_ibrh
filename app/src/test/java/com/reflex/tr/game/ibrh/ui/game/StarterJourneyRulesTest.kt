package com.reflex.tr.game.ibrh.ui.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The opening three days decide what a brand-new player sees first, and each day pays out once.
 * The day gating, the forgiveness for a missed day and the single-claim rule are pinned here.
 */
class StarterJourneyRulesTest {

    private fun dayOne(vararg claimed: Int) =
        StarterJourneyState(daysSinceStart = 1, claimedDays = claimed.toSet())

    // --- day gating ---

    @Test
    fun `starts on day one with nothing done`() {
        val state = StarterJourneyState()
        assertEquals(StarterJourneyDay.LearnTheGame, state.activeDay)
        assertTrue(state.isActive)
        assertFalse(state.isCompleted)
        assertFalse(state.hasClaimableReward)
    }

    @Test
    fun `a strong first run cannot reach into later days`() {
        val advance = advanceStarterJourneyAfterGame(dayOne(), score = 90, maxCombo = 20)
        assertTrue(advance.state.isDayCompleted(StarterJourneyDay.LearnTheGame))
        assertFalse(advance.state.isTaskCompleted(StarterTask.ReachCombo5))
        assertFalse(advance.state.isTaskCompleted(StarterTask.Score50))
    }

    @Test
    fun `the same run clears its own day once that day is unlocked`() {
        val onDayThree = StarterJourneyState(daysSinceStart = 3, claimedDays = setOf(1, 2))
        val advance = advanceStarterJourneyAfterGame(onDayThree, score = 90, maxCombo = 20)
        assertTrue(advance.state.isTaskCompleted(StarterTask.Score50))
        assertFalse(advance.state.isTaskCompleted(StarterTask.OpenLeaderboard))
    }

    @Test
    fun `a missed day is waited for rather than skipped`() {
        val late = StarterJourneyState(daysSinceStart = 3)
        assertEquals(StarterJourneyDay.LearnTheGame, late.activeDay)
    }

    @Test
    fun `a zero score run still proves the game was finished`() {
        val advance = advanceStarterJourneyAfterGame(dayOne(), score = 0, maxCombo = 0)
        assertTrue(advance.state.isTaskCompleted(StarterTask.FinishFirstGame))
        assertTrue(advance.state.isTaskCompleted(StarterTask.SeeGameOver))
        assertFalse(advance.state.isTaskCompleted(StarterTask.ScoreAnyPoint))
    }

    @Test
    fun `counting tasks need every run`() {
        var state = StarterJourneyState(daysSinceStart = 2, claimedDays = setOf(1))
        repeat(2) { state = advanceStarterJourneyAfterGame(state, score = 5, maxCombo = 0).state }
        assertFalse(state.isTaskCompleted(StarterTask.PlayThreeGames))
        assertEquals(2, state.progressOf(StarterTask.PlayThreeGames))

        state = advanceStarterJourneyAfterGame(state, score = 5, maxCombo = 0).state
        assertTrue(state.isTaskCompleted(StarterTask.PlayThreeGames))
        assertEquals(3, state.progressOf(StarterTask.PlayThreeGames))
    }

    @Test
    fun `reports only the tasks that finished on this run`() {
        val first = advanceStarterJourneyAfterGame(dayOne(), score = 4, maxCombo = 0)
        assertEquals(
            listOf(StarterTask.FinishFirstGame, StarterTask.ScoreAnyPoint, StarterTask.SeeGameOver),
            first.completedTasks
        )
        val second = advanceStarterJourneyAfterGame(first.state, score = 4, maxCombo = 0)
        assertTrue(second.completedTasks.isEmpty())
    }

    // --- one-off actions ---

    @Test
    fun `an action outside its own day changes nothing`() {
        val state = dayOne()
        val advance = advanceStarterJourneyForAction(state, StarterTask.OpenLeaderboard)
        assertSame(state, advance.state)
        assertFalse(advance.hasProgress)
    }

    @Test
    fun `repeating an action does not re-announce it`() {
        val onDayTwo = StarterJourneyState(daysSinceStart = 2, claimedDays = setOf(1))
        val first = advanceStarterJourneyForAction(onDayTwo, StarterTask.SeeDailyEvent)
        assertEquals(listOf(StarterTask.SeeDailyEvent), first.completedTasks)

        val second = advanceStarterJourneyForAction(first.state, StarterTask.SeeDailyEvent)
        assertSame(first.state, second.state)
    }

    // --- claiming ---

    @Test
    fun `nothing to claim until the whole day is done`() {
        assertNull(claimedStarterJourneyDay(dayOne()))
        val done = advanceStarterJourneyAfterGame(dayOne(), score = 10, maxCombo = 0).state
        assertTrue(done.hasClaimableReward)
        assertEquals(StarterJourneyDay.LearnTheGame, claimedStarterJourneyDay(done)?.second)
    }

    @Test
    fun `a day pays exactly once and then moves on`() {
        val done = advanceStarterJourneyAfterGame(dayOne(), score = 10, maxCombo = 0).state
        val (claimed, day) = requireNotNull(claimedStarterJourneyDay(done))
        assertEquals(StarterJourneyDay.LearnTheGame, day)
        assertNull(claimedStarterJourneyDay(claimed))
        // Day two is still locked on day one, so the card has nothing left to show today.
        assertNull(claimed.activeDay)
        assertFalse(claimed.isActive)
    }

    @Test
    fun `the journey ends only when all three days are collected`() {
        val state = StarterJourneyState(daysSinceStart = 3, claimedDays = setOf(1, 2, 3))
        assertTrue(state.isCompleted)
        assertFalse(state.isActive)
        assertNull(state.activeDay)
        assertTrue(state.showsCompletedNote)
        assertFalse(state.copy(daysSinceStart = 30).showsCompletedNote)
    }

    @Test
    fun `a reward earned inside the window survives past it`() {
        val earnedLate = advanceStarterJourneyAfterGame(
            StarterJourneyState(daysSinceStart = 3, claimedDays = setOf(1, 2)),
            score = 60,
            maxCombo = 0
        ).state
            .let { advanceStarterJourneyForAction(it, StarterTask.OpenLeaderboard).state }
            .let { advanceStarterJourneyForAction(it, StarterTask.ClaimAnyReward).state }
        assertTrue(earnedLate.hasClaimableReward)

        val nextWeek = earnedLate.copy(daysSinceStart = 9)
        assertTrue(nextWeek.isActive)
        assertEquals(StarterJourneyDay.PrepareForLeaderboard, claimedStarterJourneyDay(nextWeek)?.second)
    }

    @Test
    fun `an unfinished day stops showing once the window closes`() {
        val stale = StarterJourneyState(daysSinceStart = 9)
        assertFalse(stale.isActive)
        assertNull(claimedStarterJourneyDay(stale))
    }

    @Test
    fun `day progress reads as a safe percentage`() {
        assertEquals(0, dayOne().dayProgressPercent(StarterJourneyDay.LearnTheGame))
        val done = advanceStarterJourneyAfterGame(dayOne(), score = 10, maxCombo = 0).state
        assertEquals(100, done.dayProgressPercent(StarterJourneyDay.LearnTheGame))
    }

    @Test
    fun `every task belongs to exactly one day and every day has tasks`() {
        assertEquals(StarterTask.entries.size, StarterJourneyDay.entries.sumOf { it.tasks.size })
        assertTrue(StarterJourneyDay.entries.all { it.tasks.isNotEmpty() })
    }
}
