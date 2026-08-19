package com.reflex.tr.game.ibrh.ui.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Titles are permanent once earned and drive what the profile shows, so the boundary of every
 * condition and the stickiness of the owned set are pinned down here.
 */
class PlayerTitleRulesTest {

    // --- meetsPlayerTitleRequirement ---

    @Test
    fun `an empty progression unlocks nothing`() {
        val progression = ProgressionState()
        assertTrue(PlayerTitle.entries.none { meetsPlayerTitleRequirement(it, progression) })
    }

    @Test
    fun `the starter title lands on the very first finished game`() {
        assertFalse(meetsPlayerTitleRequirement(PlayerTitle.NewReflex, ProgressionState()))
        assertTrue(
            meetsPlayerTitleRequirement(PlayerTitle.NewReflex, ProgressionState(totalGames = 1))
        )
    }

    @Test
    fun `each condition flips exactly at its own threshold`() {
        val cases = listOf(
            PlayerTitle.ComboHunter to { v: Int -> ProgressionState(lifetimeMaxCombo = v) },
            PlayerTitle.SpeedMaster to { v: Int ->
                ProgressionState(personalRecords = PersonalRecordsState(bestScore = v))
            },
            PlayerTitle.SharpTapper to { v: Int ->
                ProgressionState(personalRecords = PersonalRecordsState(bestAccuracyPercent = v))
            },
            PlayerTitle.BossHunter to { v: Int -> ProgressionState(totalBossRoundHits = v) },
            PlayerTitle.UltraPlayer to { v: Int -> ProgressionState(totalUltraMomentHits = v) },
            PlayerTitle.LoyalPlayer to { v: Int ->
                ProgressionState(dailyReward = DailyRewardState(streakDay = v))
            },
            PlayerTitle.ReflexLegend to { v: Int -> ProgressionState(totalGames = v) }
        )
        cases.forEach { (title, build) ->
            val required = title.requirementValue
            assertFalse(title.name, meetsPlayerTitleRequirement(title, build(required - 1)))
            assertTrue(title.name, meetsPlayerTitleRequirement(title, build(required)))
        }
    }

    @Test
    fun `the collection title counts themes and skins together`() {
        val progression = ProgressionState(
            unlockedThemes = setOf(PlayerTheme.NeonRed, PlayerTheme.CyberBlue),
            unlockedTargetSkins = setOf(TargetSkin.ClassicTarget, TargetSkin.NeonRing)
        )
        assertFalse(meetsPlayerTitleRequirement(PlayerTitle.Collector, progression))
        assertTrue(
            meetsPlayerTitleRequirement(
                PlayerTitle.Collector,
                progression.copy(unlockedTargetSkins = progression.unlockedTargetSkins + TargetSkin.FireCore)
            )
        )
    }

    @Test
    fun `the league title accepts either the sticky badge or the live tier`() {
        assertTrue(
            meetsPlayerTitleRequirement(
                PlayerTitle.NeonWarrior,
                ProgressionState(neonLeagueBadgeUnlocked = true)
            )
        )
        assertTrue(
            meetsPlayerTitleRequirement(
                PlayerTitle.NeonWarrior,
                ProgressionState(weeklyLeague = WeeklyLeagueState(points = LeagueTier.Neon.minPoints))
            )
        )
        assertFalse(meetsPlayerTitleRequirement(PlayerTitle.NeonWarrior, ProgressionState()))
    }

    @Test
    fun `the loyalty badge stands in for the streak it replaces`() {
        val progression = ProgressionState(
            dailyReward = DailyRewardState(streakDay = 1, loyalBadgeUnlocked = true)
        )
        assertTrue(meetsPlayerTitleRequirement(PlayerTitle.LoyalPlayer, progression))
    }

    @Test
    fun `progress never reads as negative`() {
        val progression = ProgressionState(totalGames = -5, lifetimeMaxCombo = -2)
        assertTrue(PlayerTitle.entries.all { playerTitleProgressValue(it, progression) >= 0 })
    }

    // --- refreshedPlayerTitles ---

    @Test
    fun `returns the very same profile when nothing moved`() {
        val profile = PlayerProfile(
            title = PlayerTitle.NewReflex,
            unlockedTitles = setOf(PlayerTitle.NewReflex)
        )
        val result = refreshedPlayerTitles(profile, ProgressionState(totalGames = 1))
        assertSame(profile, result.profile)
        assertTrue(result.newlyUnlocked.isEmpty())
    }

    @Test
    fun `a first game unlocks and auto-selects the starter title`() {
        val result = refreshedPlayerTitles(PlayerProfile(), ProgressionState(totalGames = 1))
        assertEquals(listOf(PlayerTitle.NewReflex), result.newlyUnlocked)
        assertEquals(PlayerTitle.NewReflex, result.profile.title)
        assertEquals(PlayerTitle.NewReflex, result.profile.activeTitle)
    }

    @Test
    fun `keeps an earned title after the streak that earned it breaks`() {
        val earned = refreshedPlayerTitles(
            PlayerProfile(),
            ProgressionState(totalGames = 1, dailyReward = DailyRewardState(streakDay = 7))
        ).profile
        assertTrue(PlayerTitle.LoyalPlayer in earned.unlockedTitles)

        val afterBreak = refreshedPlayerTitles(earned, ProgressionState(totalGames = 1))
        assertTrue(PlayerTitle.LoyalPlayer in afterBreak.profile.unlockedTitles)
        assertTrue(afterBreak.newlyUnlocked.isEmpty())
    }

    @Test
    fun `never re-announces a title that is already owned`() {
        val progression = ProgressionState(totalGames = 120)
        val first = refreshedPlayerTitles(PlayerProfile(), progression)
        assertTrue(PlayerTitle.ReflexLegend in first.newlyUnlocked)

        val second = refreshedPlayerTitles(first.profile, progression)
        assertTrue(second.newlyUnlocked.isEmpty())
    }

    @Test
    fun `does not move a title the player chose on purpose`() {
        val profile = PlayerProfile(
            title = PlayerTitle.NewReflex,
            unlockedTitles = setOf(PlayerTitle.NewReflex)
        )
        val result = refreshedPlayerTitles(profile, ProgressionState(totalGames = 120))
        assertTrue(PlayerTitle.ReflexLegend in result.profile.unlockedTitles)
        assertEquals(PlayerTitle.NewReflex, result.profile.title)
    }

    @Test
    fun `repairs a stored title the player does not own`() {
        val profile = PlayerProfile(title = PlayerTitle.ReflexLegend, unlockedTitles = emptySet())
        assertNull(profile.activeTitle)

        val result = refreshedPlayerTitles(profile, ProgressionState(totalGames = 1))
        assertEquals(PlayerTitle.NewReflex, result.profile.title)
    }

    @Test
    fun `picks the rarest owned title when nothing is active`() {
        assertNull(bestPlayerTitle(emptySet()))
        assertEquals(
            PlayerTitle.ReflexLegend,
            bestPlayerTitle(setOf(PlayerTitle.NewReflex, PlayerTitle.ComboHunter, PlayerTitle.ReflexLegend))
        )
    }
}
