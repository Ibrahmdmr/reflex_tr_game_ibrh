package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Round-trip coverage for the persistence layer. Eight collections are stored as comma-joined
 * strings, and a defect there silently wipes the player's economy, so each is written and read back.
 */
@RunWith(RobolectricTestRunner::class)
class GamePreferencesTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        context.getSharedPreferences("game_preferences", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    /** A fresh instance, to prove values survive the object and come back from storage. */
    private fun reopen() = GamePreferences(context)

    // --- wallet and totals ---

    @Test
    fun `restores the wallet and lifetime totals`() {
        val saved = reopen().getProgressionState().copy(
            coins = 1234,
            totalCoinsEarned = 5000,
            totalCoinsSpent = 3766,
            xp = 780,
            totalGames = 42,
            totalScore = 999,
            totalHits = 300,
            totalMisses = 120,
            lifetimeMaxCombo = 17,
            rewardedAdWatchCount = 9
        )
        reopen().saveProgressionState(saved)

        val loaded = reopen().getProgressionState()
        assertEquals(1234, loaded.coins)
        assertEquals(5000, loaded.totalCoinsEarned)
        assertEquals(3766, loaded.totalCoinsSpent)
        assertEquals(780, loaded.xp)
        assertEquals(42, loaded.totalGames)
        assertEquals(999, loaded.totalScore)
        assertEquals(300, loaded.totalHits)
        assertEquals(120, loaded.totalMisses)
        assertEquals(17, loaded.lifetimeMaxCombo)
        assertEquals(9, loaded.rewardedAdWatchCount)
    }

    @Test
    fun `never restores a negative coin balance`() {
        reopen().saveProgressionState(reopen().getProgressionState().copy(coins = -500))

        assertTrue(reopen().getProgressionState().coins >= 0)
    }

    // --- comma-joined collections ---

    @Test
    fun `restores every unlocked theme`() {
        val themes = setOf(PlayerTheme.NeonRed, PlayerTheme.CyberBlue, PlayerTheme.LavaCore)
        reopen().saveProgressionState(
            reopen().getProgressionState().copy(unlockedThemes = themes, selectedTheme = PlayerTheme.LavaCore)
        )

        val loaded = reopen().getProgressionState()
        assertEquals(themes, loaded.unlockedThemes)
        assertEquals(PlayerTheme.LavaCore, loaded.selectedTheme)
    }

    @Test
    fun `restores every unlocked target skin`() {
        val skins = setOf(TargetSkin.ClassicTarget, TargetSkin.NeonRing)
        reopen().saveProgressionState(
            reopen().getProgressionState().copy(
                unlockedTargetSkins = skins,
                selectedTargetSkin = TargetSkin.NeonRing
            )
        )

        val loaded = reopen().getProgressionState()
        assertEquals(skins, loaded.unlockedTargetSkins)
        assertEquals(TargetSkin.NeonRing, loaded.selectedTargetSkin)
    }

    @Test
    fun `restores an earned achievement as claimed`() {
        // Achievement progress is derived from lifetime stats, so the run count is what unlocks
        // "play_10" — the claimed flag alone is not enough to restore it.
        val before = reopen().getProgressionState()
        reopen().saveProgressionState(
            before.copy(
                totalGames = 100,
                achievements = before.achievements.map { it.copy(claimed = it.id == "play_10") }
            )
        )

        val loaded = reopen().getProgressionState().achievements.single { it.id == "play_10" }
        assertTrue("A reached achievement must come back unlocked", loaded.unlocked)
        assertTrue("Its claimed flag must survive the round trip", loaded.claimed)
    }

    @Test
    fun `drops a claimed flag on an achievement that was never earned`() {
        // Data-integrity guard: storage must not be able to hand back a reward the player has
        // not reached, however the flag got written.
        val before = reopen().getProgressionState()
        reopen().saveProgressionState(
            before.copy(
                totalGames = 0,
                achievements = before.achievements.map { it.copy(claimed = it.id == "play_100") }
            )
        )

        val loaded = reopen().getProgressionState().achievements.single { it.id == "play_100" }
        assertFalse(loaded.unlocked)
        assertFalse(loaded.claimed)
    }

    @Test
    fun `restores the selected profile badges`() {
        val badges = listOf(ProfileBadge.FirstGame.storageKey, ProfileBadge.RecordBreaker.storageKey)
        reopen().saveProgressionState(
            reopen().getProgressionState().copy(selectedProfileBadgeIds = badges)
        )

        assertEquals(badges.toSet(), reopen().getProgressionState().selectedProfileBadgeIds.toSet())
    }

    // --- season ---

    @Test
    fun `restores season levels missions and cosmetics`() {
        val before = reopen().getProgressionState()
        val season = before.season.copy(
            seasonNumber = 3,
            xp = 640,
            claimedRewardLevels = setOf(1, 4, 7),
            preservedBadgeLevels = setOf(2, 5),
            claimedMissionIds = setOf("play_3", "rewarded_1"),
            usedCosmeticKeys = setOf("neon_red", "cyber_blue")
        )
        reopen().saveProgressionState(before.copy(season = season))

        val loaded = reopen().getProgressionState().season
        assertEquals(3, loaded.seasonNumber)
        assertEquals(640, loaded.xp)
        assertEquals(setOf(1, 4, 7), loaded.claimedRewardLevels)
        assertEquals(setOf(2, 5), loaded.preservedBadgeLevels)
        assertEquals(setOf("play_3", "rewarded_1"), loaded.claimedMissionIds)
        assertEquals(setOf("neon_red", "cyber_blue"), loaded.usedCosmeticKeys)
    }

    @Test
    fun `restores a completed season quest as claimed`() {
        val before = reopen().getProgressionState()
        val quest = before.season.quests.first()
        reopen().saveProgressionState(
            before.copy(
                season = before.season.copy(
                    quests = before.season.quests.map {
                        if (it.type == quest.type) {
                            it.copy(progress = it.type.target, claimed = true)
                        } else {
                            it
                        }
                    }
                )
            )
        )

        val loaded = reopen().getProgressionState().season.quests.single { it.type == quest.type }
        assertEquals(quest.type.target, loaded.progress)
        assertTrue(loaded.claimed)
    }

    @Test
    fun `drops a claimed flag on an unfinished season quest`() {
        val before = reopen().getProgressionState()
        val quest = before.season.quests.first()
        reopen().saveProgressionState(
            before.copy(
                season = before.season.copy(
                    quests = before.season.quests.map {
                        if (it.type == quest.type) it.copy(progress = 0, claimed = true) else it
                    }
                )
            )
        )

        assertFalse(
            reopen().getProgressionState().season.quests.single { it.type == quest.type }.claimed
        )
    }

    // --- personal records ---

    @Test
    fun `restores personal records`() {
        val records = PersonalRecordsState(
            bestScore = 88,
            bestCombo = 21,
            bestAccuracyPercent = 93,
            longestSurvivalSeconds = 47,
            mostCoinsInGame = 610
        )
        reopen().saveProgressionState(reopen().getProgressionState().copy(personalRecords = records))

        assertEquals(records, reopen().getProgressionState().personalRecords)
    }

    // --- best scores ---

    @Test
    fun `keeps a separate best score per mode`() = runTest {
        val preferences = reopen()
        preferences.saveBestScore(GameMode.Classic, 30)
        preferences.saveBestScore(GameMode.FakeTarget, 12)

        val best = reopen().bestScoresFlow.first()
        assertEquals(30, best[GameMode.Classic])
        assertEquals(12, best[GameMode.FakeTarget])
        assertEquals(0, best[GameMode.MovingTarget])
    }

    @Test
    fun `never lowers a best score`() = runTest {
        val preferences = reopen()
        preferences.saveBestScore(GameMode.Classic, 40)
        preferences.saveBestScore(GameMode.Classic, 25)

        assertEquals(40, reopen().bestScoresFlow.first()[GameMode.Classic])
    }

    // --- player profile ---

    @Test
    fun `restores the player name and marks the prompt as done`() {
        reopen().savePlayerName("Blitz")

        val profile = reopen().getPlayerProfile()
        assertEquals("Blitz", profile.name)
        assertTrue(profile.hasCompletedNamePrompt)
    }

    @Test
    fun `treats an unnamed player as not yet prompted`() {
        val profile = reopen().getPlayerProfile()

        assertEquals("", profile.name)
        assertFalse(profile.hasCompletedNamePrompt)
    }

    @Test
    fun `trims and caps a long player name`() {
        reopen().savePlayerName("   ThisNameIsFarTooLong   ")

        assertEquals(12, reopen().getPlayerProfile().name.length)
    }

    // --- daily challenge and mode tips ---

    @Test
    fun `restores the daily challenge state`() {
        val challenge = DailyChallengeState.default().copy(
            progress = 7,
            completed = true,
            rewardClaimed = true,
            doubleRewardClaimed = true
        )
        reopen().saveDailyChallengeState(challenge)

        val loaded = reopen().getDailyChallengeState()
        assertEquals(7, loaded.progress)
        assertTrue(loaded.completed)
        assertTrue(loaded.rewardClaimed)
        assertTrue(loaded.doubleRewardClaimed)
    }

    @Test
    fun `remembers which mode tips were shown and can reset them`() {
        val preferences = reopen()
        preferences.markModeTipShown(GameMode.Classic)
        preferences.markModeTipShown(GameMode.ColorReflex)

        assertEquals(
            setOf(GameMode.Classic, GameMode.ColorReflex),
            reopen().getShownModeTips()
        )

        reopen().resetModeTips()
        assertTrue(reopen().getShownModeTips().isEmpty())
    }
}
