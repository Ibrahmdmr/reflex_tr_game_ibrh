package com.reflex.tr.game.ibrh.ui.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The season layer: XP boost, season XP, quests and mode mastery. Clock reads are injected where
 * the production code allows it; the rest is asserted as invariants that hold on any day.
 */
class SeasonProgressionTest {

    private val now = 1_700_000_000_000L

    // --- XP boost projection ---

    @Test
    fun `projects the minutes left on an active boost`() {
        val season = SeasonState(xpBoostEndTimeMillis = now + 30 * 60_000L)

        assertEquals(30, season.withRefreshedXpBoost(now).xpBoostRemainingMinutes)
    }

    @Test
    fun `rounds a part-minute up so a live boost never reads as zero`() {
        // A boost with one second left is still a boost; rounding down would show "0 minutes"
        // next to an active badge.
        val season = SeasonState(xpBoostEndTimeMillis = now + 1_000L)

        val refreshed = season.withRefreshedXpBoost(now)
        assertEquals(1, refreshed.xpBoostRemainingMinutes)
        assertTrue(refreshed.isXpBoostActive)
    }

    @Test
    fun `reports an expired boost as inactive`() {
        val season = SeasonState(
            xpBoostEndTimeMillis = now - 1L,
            xpBoostRemainingMinutes = 5
        )

        val refreshed = season.withRefreshedXpBoost(now)
        assertEquals(0, refreshed.xpBoostRemainingMinutes)
        assertFalse(refreshed.isXpBoostActive)
    }

    @Test
    fun `treats a season without a boost as inactive`() {
        assertFalse(SeasonState().withRefreshedXpBoost(now).isXpBoostActive)
    }

    @Test
    fun `keeps the same instance when the minute has not turned over`() {
        // The minute ticker calls this every 60 seconds. Returning an equal-but-new instance would
        // recompose the home screen on every tick for nothing.
        val season = SeasonState(xpBoostEndTimeMillis = now + 30 * 60_000L)
            .withRefreshedXpBoost(now)

        assertSame(season, season.withRefreshedXpBoost(now + 500L))
    }

    @Test
    fun `returns a new instance once the minute turns over`() {
        val season = SeasonState(xpBoostEndTimeMillis = now + 30 * 60_000L)
            .withRefreshedXpBoost(now)

        assertEquals(29, season.withRefreshedXpBoost(now + 60_000L).xpBoostRemainingMinutes)
    }

    // --- claimable rewards ---

    @Test
    fun `reports a reached but uncollected reward as claimable`() {
        val season = SeasonState(xp = SeasonXpPerLevel * 3)

        assertTrue("Level ${season.level} with nothing claimed must have rewards waiting", season.hasClaimableReward)
    }

    @Test
    fun `reports nothing claimable once every reached level is collected`() {
        val season = SeasonState(
            xp = SeasonXpPerLevel * 3,
            claimedRewardLevels = (1..4).toSet()
        )

        assertFalse(season.hasClaimableReward)
    }

    @Test
    fun `ignores rewards above the current level`() {
        // Claiming level 1 clears everything reachable at level 1; levels 2+ are not yet earned.
        val season = SeasonState(xp = 0, claimedRewardLevels = setOf(1))

        assertEquals(1, season.level)
        assertFalse(season.hasClaimableReward)
    }

    @Test
    fun `agrees with the reward list it replaced`() {
        // The list-building form this shortcut replaced, kept as the reference implementation.
        listOf(0, SeasonXpPerLevel * 2, SeasonXpPerLevel * 9, SeasonXpPerLevel * SeasonMaxLevel).forEach { xp ->
            listOf(emptySet(), setOf(1), setOf(1, 2, 3), (1..10).toSet()).forEach { claimed ->
                val season = SeasonState(xp = xp, claimedRewardLevels = claimed)
                val viaList = season.rewards.any { it.level <= season.level && !it.claimed }
                assertEquals("xp=$xp claimed=$claimed", viaList, season.hasClaimableReward)
            }
        }
    }

    // --- season XP ---

    @Test
    fun `pays the boost bonus while the boost is running`() {
        val progression = progressionWithSeason(
            SeasonState(missionDateKey = todayDateKey(), xpBoostEndTimeMillis = now + 60_000L)
        )

        val awarded = addSeasonXp(progression, xpAmount = 100, currentTimeMillis = now)
        assertEquals(100 + SeasonXpBoostBonusPercent, awarded.season.xp)
    }

    @Test
    fun `pays the plain amount once the boost has expired`() {
        val progression = progressionWithSeason(
            SeasonState(missionDateKey = todayDateKey(), xpBoostEndTimeMillis = now - 1L)
        )

        assertEquals(100, addSeasonXp(progression, xpAmount = 100, currentTimeMillis = now).season.xp)
    }

    @Test
    fun `ignores a non-positive award`() {
        val progression = progressionWithSeason(SeasonState(xp = 40))

        assertSame(progression, addSeasonXp(progression, xpAmount = 0))
        assertSame(progression, addSeasonXp(progression, xpAmount = -25))
    }

    @Test
    fun `never lets season xp pass the last level`() {
        val maxXp = (SeasonMaxLevel - 1) * SeasonXpPerLevel
        val progression = progressionWithSeason(
            SeasonState(missionDateKey = todayDateKey(), xp = maxXp - 10)
        )

        val awarded = addSeasonXp(progression, xpAmount = 10_000, currentTimeMillis = now).season
        assertEquals(maxXp, awarded.xp)
        assertEquals(SeasonMaxLevel, awarded.level)
    }

    @Test
    fun `counts a played game and a watched ad only when asked`() {
        val progression = progressionWithSeason(SeasonState(missionDateKey = todayDateKey()))

        val played = addSeasonXp(progression, 10, countGamePlayed = true, currentTimeMillis = now).season
        assertEquals(1, played.gamesPlayedToday)
        assertEquals(0, played.rewardedAdsWatchedToday)

        val watched = addSeasonXp(progression, 10, countRewardedAd = true, currentTimeMillis = now).season
        assertEquals(0, watched.gamesPlayedToday)
        assertEquals(1, watched.rewardedAdsWatchedToday)
    }

    @Test
    fun `resets the daily mission counters on a new day`() {
        val yesterday = SeasonState(
            missionDateKey = "2000-01-01",
            xp = 500,
            gamesPlayedToday = 7,
            rewardedAdsWatchedToday = 3,
            seasonXpEarnedToday = 400,
            claimedMissionIds = setOf("play_3_games")
        )

        val today = seasonForToday(yesterday)
        assertEquals(todayDateKey(), today.missionDateKey)
        assertEquals(0, today.gamesPlayedToday)
        assertEquals(0, today.rewardedAdsWatchedToday)
        assertEquals(0, today.seasonXpEarnedToday)
        assertTrue(today.claimedMissionIds.isEmpty())
        assertEquals("Season xp is not a daily counter", 500, today.xp)
    }

    // --- season quests ---

    @Test
    fun `advances the play and score quests after a game`() {
        val season = advanceSeasonQuestsAfterGame(
            season = SeasonState(missionDateKey = todayDateKey()),
            score = 40,
            maxCombo = 4,
            theme = PlayerTheme.NeonRed,
            targetSkin = TargetSkin.ClassicTarget
        )

        assertEquals(1, season.questProgress(SeasonQuestType.Play100Games))
        assertEquals(40, season.questProgress(SeasonQuestType.Score3000))
        assertEquals("A combo below 10 must not count", 0, season.questProgress(SeasonQuestType.Combo10TwentyFiveTimes))
    }

    @Test
    fun `counts a game with a combo of ten towards the combo quest`() {
        val season = advanceSeasonQuestsAfterGame(
            season = SeasonState(missionDateKey = todayDateKey()),
            score = 10,
            maxCombo = 10,
            theme = PlayerTheme.NeonRed,
            targetSkin = TargetSkin.ClassicTarget
        )

        assertEquals(1, season.questProgress(SeasonQuestType.Combo10TwentyFiveTimes))
    }

    @Test
    fun `records the theme and skin worn during the game as used cosmetics`() {
        val season = advanceSeasonQuestsAfterGame(
            season = SeasonState(missionDateKey = todayDateKey()),
            score = 5,
            maxCombo = 2,
            theme = PlayerTheme.CyberBlue,
            targetSkin = TargetSkin.NeonRing
        )

        assertTrue("theme:${PlayerTheme.CyberBlue.storageKey}" in season.usedCosmeticKeys)
        assertTrue("skin:${TargetSkin.NeonRing.storageKey}" in season.usedCosmeticKeys)
        assertEquals(2, season.questProgress(SeasonQuestType.Use5Cosmetics))
    }

    @Test
    fun `claims a finished quest exactly once`() {
        var season = SeasonState(
            missionDateKey = todayDateKey(),
            quests = questsWith(SeasonQuestType.Play100Games, progress = 99)
        )

        season = advanceSeasonQuestsAfterGame(
            season = season,
            score = 1,
            maxCombo = 1,
            theme = PlayerTheme.NeonRed,
            targetSkin = TargetSkin.ClassicTarget
        )
        val completed = season.quests.single { it.type == SeasonQuestType.Play100Games }
        assertTrue(completed.claimed)
        assertTrue("The reward is paid on the game that finished it", completed.rewardClaimedThisGame)

        season = advanceSeasonQuestsAfterGame(
            season = season,
            score = 1,
            maxCombo = 1,
            theme = PlayerTheme.NeonRed,
            targetSkin = TargetSkin.ClassicTarget
        )
        assertFalse(
            "An already-claimed quest must not pay again",
            season.quests.single { it.type == SeasonQuestType.Play100Games }.rewardClaimedThisGame
        )
    }

    @Test
    fun `never lets quest progress pass its target`() {
        val season = advanceSeasonQuestsAfterGame(
            season = SeasonState(missionDateKey = todayDateKey()),
            score = 99_999,
            maxCombo = 50,
            theme = PlayerTheme.NeonRed,
            targetSkin = TargetSkin.ClassicTarget
        )

        season.quests.forEach {
            assertTrue("${it.type} passed its target", it.progress <= it.target)
        }
    }

    @Test
    fun `pays the quest reward and unlocks the badge on the final daily mission`() {
        val progression = progressionWithSeason(
            SeasonState(
                missionDateKey = todayDateKey(),
                quests = SeasonQuestType.entries.map { type ->
                    SeasonQuestState(
                        type = type,
                        progress = if (type == SeasonQuestType.Complete10DailyMissions) {
                            type.target - 1
                        } else {
                            type.target
                        },
                        claimed = type != SeasonQuestType.Complete10DailyMissions
                    )
                }
            )
        ).copy(coins = 0)

        val updated = advanceSeasonQuestForDailyMissionClaim(progression)
        assertEquals(SeasonQuestType.Complete10DailyMissions.rewardCoins, updated.coins)
        assertTrue(updated.season.seasonQuestsCompleted)
        assertTrue("Finishing every quest unlocks the season hunter badge", updated.seasonHunterBadgeUnlocked)
    }

    // --- mode mastery ---

    @Test
    fun `scales mastery xp with score and combo within their caps`() {
        assertEquals(20, calculateModeMasteryXp(score = 0, maxCombo = 0))
        assertEquals(20 + 15 + 4, calculateModeMasteryXp(score = 15, maxCombo = 4))
        assertEquals(
            "Score caps at 30 and combo at 10",
            20 + 30 + 10,
            calculateModeMasteryXp(score = 5_000, maxCombo = 500)
        )
    }

    @Test
    fun `adds mastery xp to the played mode alone`() {
        val progression = ProgressionState(modeMasteryXpByMode = mapOf(GameMode.Classic to 50))

        val result = advanceModeMastery(progression, GameMode.Classic, score = 10, maxCombo = 2)
        assertEquals(50 + calculateModeMasteryXp(10, 2), result.xpByMode[GameMode.Classic])
        assertNull(result.xpByMode[GameMode.MovingTarget])
    }

    @Test
    fun `reports no level up when the game does not cross a level`() {
        val progression = ProgressionState(modeMasteryXpByMode = mapOf(GameMode.Classic to 0))

        val result = advanceModeMastery(progression, GameMode.Classic, score = 0, maxCombo = 0)
        assertNull(result.levelUp)
        assertEquals(0, result.coinBonus)
    }

    @Test
    fun `pays the level bonus when a game crosses a level`() {
        val progression = ProgressionState(
            modeMasteryXpByMode = mapOf(GameMode.Classic to MODE_MASTERY_XP_PER_LEVEL - 1)
        )

        val result = advanceModeMastery(progression, GameMode.Classic, score = 30, maxCombo = 10)
        val levelUp = requireNotNull(result.levelUp)
        assertEquals(GameMode.Classic, levelUp.mode)
        assertEquals(2, levelUp.level)
        assertEquals(modeMasteryLevelReward(2), result.coinBonus)
    }

    @Test
    fun `never lets mastery xp pass the last level`() {
        val maxXp = (MODE_MASTERY_MAX_LEVEL - 1) * MODE_MASTERY_XP_PER_LEVEL
        val progression = ProgressionState(modeMasteryXpByMode = mapOf(GameMode.Classic to maxXp))

        val result = advanceModeMastery(progression, GameMode.Classic, score = 30, maxCombo = 10)
        assertEquals(maxXp, result.xpByMode[GameMode.Classic])
        assertNull("A capped mode cannot level up again", result.levelUp)
    }

    @Test
    fun `pays a bigger bonus on the milestone levels`() {
        assertEquals(300, modeMasteryLevelReward(5))
        assertEquals(750, modeMasteryLevelReward(10))
        assertEquals(100, modeMasteryLevelReward(2))
    }

    // --- daily mini tournament ---

    @Test
    fun `only advances the tournament for the mode of the day`() {
        val tournament = createDailyMiniTournamentState()
        val otherMode = GameMode.entries.first { it != tournament.mode }

        val untouched = advanceDailyMiniTournamentAfterGame(tournament, otherMode, score = 9_999)
        assertEquals(0, untouched.bestScore)
        assertFalse(untouched.completed)
    }

    @Test
    fun `keeps the best score and pays the reward once the target is reached`() {
        val tournament = createDailyMiniTournamentState()

        val reached = advanceDailyMiniTournamentAfterGame(
            tournament = tournament,
            playedMode = tournament.mode,
            score = tournament.targetScore
        )
        assertEquals(tournament.targetScore, reached.bestScore)
        assertTrue(reached.completed)
        assertTrue(reached.rewardClaimedThisGame)

        val worseRun = advanceDailyMiniTournamentAfterGame(reached, tournament.mode, score = 1)
        assertEquals("A worse run must not lower the best score", tournament.targetScore, worseRun.bestScore)
        assertFalse("The reward is paid only once", worseRun.rewardClaimedThisGame)
    }

    @Test
    fun `pays nothing for a run below the target`() {
        val tournament = createDailyMiniTournamentState()

        assertEquals(
            0,
            dailyMiniTournamentRewardForGame(tournament, tournament.mode, score = tournament.targetScore - 1)
        )
        assertTrue(
            dailyMiniTournamentRewardForGame(tournament, tournament.mode, score = tournament.targetScore) > 0
        )
    }

    // --- daily featured mode ---

    @Test
    fun `picks the same featured mode for the same day`() {
        assertEquals(createDailyFeaturedMode("2026-03-14").mode, createDailyFeaturedMode("2026-03-14").mode)
    }

    @Test
    fun `pays the featured bonus only for the featured mode`() {
        val featured = createDailyFeaturedMode("2026-03-14")
        val otherMode = GameMode.entries.first { it != featured.mode }

        assertEquals(20, calculateDailyModeBonusCoins(100, featured.mode, featured))
        assertEquals(0, calculateDailyModeBonusCoins(100, otherMode, featured))
    }

    @Test
    fun `rounds a small featured bonus up to one coin`() {
        val featured = createDailyFeaturedMode("2026-03-14")

        assertEquals("A bonus must never round away to nothing", 1, calculateDailyModeBonusCoins(1, featured.mode, featured))
        assertEquals("No coins earned means no bonus", 0, calculateDailyModeBonusCoins(0, featured.mode, featured))
    }

    // --- helpers ---

    private fun progressionWithSeason(season: SeasonState) = ProgressionState(season = season)

    private fun SeasonState.questProgress(type: SeasonQuestType) =
        quests.single { it.type == type }.progress

    private fun questsWith(type: SeasonQuestType, progress: Int) =
        SeasonQuestType.entries.map { entry ->
            SeasonQuestState(type = entry, progress = if (entry == type) progress else 0)
        }
}
