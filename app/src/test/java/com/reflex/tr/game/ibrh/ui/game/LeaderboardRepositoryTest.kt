package com.reflex.tr.game.ibrh.ui.game

import com.reflex.tr.game.ibrh.R
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The offline leaderboard and the rank tiers. The Firestore repository needs live Play services
 * and is exercised on device instead (releaseTest variant).
 */
class LeaderboardRepositoryTest {

    private val repository = LocalLeaderboardRepository()

    private fun snapshot(
        name: String = "Blitz",
        score: Int = 20,
        mode: GameMode = GameMode.Classic,
        period: LeaderboardPeriod = LeaderboardPeriod.AllTime,
        tick: Int = 0
    ) = repository.getLocalLeaderboard(
        playerName = name,
        playerScore = score,
        playerTheme = PlayerTheme.NeonRed,
        playerRankTier = RankTier.Bronze,
        selectedMode = mode,
        selectedPeriod = period,
        refreshTick = tick
    )

    // --- ordering and the player's row ---

    @Test
    fun `always places the player somewhere in the table`() {
        val entries = snapshot().entries

        assertEquals("Exactly one row is the player", 1, entries.count { it.isPlayer })
    }

    @Test
    fun `orders entries from the highest score down`() {
        val scores = snapshot().entries.map { it.score }

        assertEquals(scores.sortedDescending(), scores)
    }

    @Test
    fun `numbers the ranks from one without gaps`() {
        val ranks = snapshot().entries.map { it.rank }

        assertEquals((1..ranks.size).toList(), ranks)
    }

    @Test
    fun `reports the player rank that matches the player row`() {
        val result = snapshot()
        val playerRow = result.entries.single { it.isPlayer }

        assertEquals(playerRow.rank, result.playerRank)
    }

    @Test
    fun `caps the table at eight rows`() {
        assertTrue(snapshot().entries.size <= 8)
    }

    @Test
    fun `scales rivals to the player's score instead of using fixed values`() {
        // The offline table is a motivational stand-in: opponents are anchored around the
        // player's own score, so the ladder stays meaningful at any skill level.
        val low = snapshot(score = 10).entries.filterNot { it.isPlayer }.map { it.score }
        val high = snapshot(score = 5_000).entries.filterNot { it.isPlayer }.map { it.score }

        assertTrue("Rivals must follow a low score", low.max() < 100)
        assertTrue("Rivals must follow a high score", high.min() > 1_000)
    }

    // --- player input is sanitised ---

    @Test
    fun `clamps a negative score to zero`() {
        val playerRow = snapshot(score = -50).entries.single { it.isPlayer }

        assertEquals(0, playerRow.score)
    }

    @Test
    fun `falls back to a default label for a blank name`() {
        val playerRow = snapshot(name = "   ").entries.single { it.isPlayer }

        assertTrue("A blank name must not reach the table", playerRow.name.isNotBlank())
    }

    @Test
    fun `caps a long player name at twelve characters`() {
        val playerRow = snapshot(name = "A".repeat(40)).entries.single { it.isPlayer }

        assertEquals(12, playerRow.name.length)
    }

    // --- determinism ---

    @Test
    fun `returns the same table for the same inputs`() {
        val first = snapshot(tick = 3)
        val second = snapshot(tick = 3)

        assertEquals(first.entries, second.entries)
        assertEquals(first.playerRank, second.playerRank)
    }

    @Test
    fun `keeps the requested mode and period on the snapshot`() {
        val result = snapshot(mode = GameMode.ColorReflex, period = LeaderboardPeriod.Weekly)

        assertEquals(GameMode.ColorReflex, result.selectedMode)
        assertEquals(LeaderboardPeriod.Weekly, result.selectedPeriod)
    }

    // --- motivation line ---

    @Test
    fun `matches the motivation line to the player's actual rank`() {
        listOf(0, 25, 400, 10_000).forEach { score ->
            val result = snapshot(score = score)
            val expected = if (result.playerRank <= 3) {
                R.string.leaderboard_motivation_top3
            } else {
                R.string.leaderboard_motivation_pass_player
            }
            assertEquals("score=$score rank=${result.playerRank}", expected, result.motivationRes)
        }
    }

    @Test
    fun `points a trailing player at the rival just ahead`() {
        val result = snapshot(score = 0)

        assertEquals(R.string.leaderboard_motivation_pass_player, result.motivationRes)
        assertTrue("A rival's name is needed for the message", result.motivationPlayerName.isNotBlank())
        assertTrue("Catching up always costs at least one point", result.motivationScoreGap >= 1)
    }

    @Test
    fun `quotes a gap that actually clears the rival`() {
        val result = snapshot(score = 0)
        val rival = result.entries.single { it.name == result.motivationPlayerName }
        val playerScore = result.entries.single { it.isPlayer }.score

        assertTrue(
            "Closing the quoted gap must overtake the rival",
            playerScore + result.motivationScoreGap > rival.score
        )
    }

    // --- offline behaviour ---

    @Test
    fun `never claims to upload a score while offline`() = runTest {
        val uploaded = repository.uploadScore(
            playerName = "Blitz",
            score = 40,
            level = 3,
            selectedTheme = PlayerTheme.NeonRed,
            mode = GameMode.Classic
        )

        assertFalse(uploaded)
    }

    // --- rank tiers ---

    @Test
    fun `maps each level band to its rank tier`() {
        assertEquals(RankTier.Bronze, rankFor(level = 1))
        assertEquals(RankTier.Bronze, rankFor(level = 4))
        assertEquals(RankTier.Silver, rankFor(level = 5))
        assertEquals(RankTier.Gold, rankFor(level = 10))
        assertEquals(RankTier.Platinum, rankFor(level = 15))
        assertEquals(RankTier.NeonMaster, rankFor(level = 25))
        assertEquals(RankTier.ReflexGod, rankFor(level = 40))
    }

    @Test
    fun `treats a level below one as the lowest tier`() {
        assertEquals(RankTier.Bronze, rankFor(level = 0))
        assertEquals(RankTier.Bronze, rankFor(level = -7))
    }
}
