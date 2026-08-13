package com.reflex.tr.game.ibrh.ui.game

import com.reflex.tr.game.ibrh.R

fun storePreviewUiState(base: GameUiState): GameUiState {
    val bestScores = mapOf(
        GameMode.Classic to 96,
        GameMode.MovingTarget to 74,
        GameMode.FakeTarget to 61,
        GameMode.ColorReflex to 82
    )
    val profile = PlayerProfile(
        name = "ReflexX",
        title = PlayerTitle.NeonLegend,
        weeklyBestScore = 88,
        weeklyBestScoresByMode = bestScores,
        hasCompletedNamePrompt = true
    )
    val progression = storePreviewProgression()
    return base.copy(
        score = 42,
        bestScore = bestScores[base.selectedMode] ?: bestScores.getValue(GameMode.Classic),
        bestScoresByMode = bestScores,
        selectedMode = GameMode.Classic,
        dailyFeaturedMode = DailyFeaturedModeState(
            dateKey = "store-preview",
            mode = GameMode.ColorReflex,
            coinBonusPercent = 20
        ),
        dailyChallengeState = DailyChallengeState(
            id = "preview_score_20",
            type = DailyChallenge.ClassicScore20,
            target = 20,
            progress = 20,
            completed = true,
            createdDate = "store-preview",
            rewardCoins = 120,
            rewardClaimed = true,
            doubleRewardClaimed = false
        ),
        progressionState = progression,
        playerProfile = profile,
        leaderboardSnapshot = storePreviewLeaderboard(profile, progression, bestScores),
        earnedCoinsThisGame = 180,
        baseCoinsThisGame = 90,
        combo = 12,
        maxCombo = 26,
        successfulHits = 42,
        totalAttempts = 45,
        isStorePreviewMode = true,
        shouldAutoShowDailyRewardDialog = false
    )
}

private fun storePreviewProgression(): ProgressionState {
    val unlockedThemes = setOf(
        PlayerTheme.NeonRed,
        PlayerTheme.CyberBlue,
        PlayerTheme.PurpleStorm,
        PlayerTheme.IceNeon,
        PlayerTheme.LavaCore,
        PlayerTheme.ToxicPulse,
        PlayerTheme.GoldFire
    )
    return ProgressionState(
        coins = 74250,
        totalCoinsEarned = 128400,
        xp = 5_860,
        level = 24,
        totalGames = 186,
        totalScore = 8_940,
        gamesPlayedByMode = mapOf(
            GameMode.Classic to 78,
            GameMode.MovingTarget to 42,
            GameMode.FakeTarget to 31,
            GameMode.ColorReflex to 35
        ),
        modeMasteryXpByMode = mapOf(
            GameMode.Classic to 640,
            GameMode.MovingTarget to 420,
            GameMode.FakeTarget to 310,
            GameMode.ColorReflex to 360
        ),
        totalHits = 4_820,
        lifetimeMaxCombo = 34,
        rewardedAdWatchCount = 28,
        selectedTheme = PlayerTheme.ToxicPulse,
        unlockedThemes = unlockedThemes,
        selectedTargetSkin = TargetSkin.FireCore,
        unlockedTargetSkins = setOf(TargetSkin.ClassicTarget, TargetSkin.NeonRing, TargetSkin.FireCore),
        coinChest = CoinChestState(
            openedToday = 1,
            maxOpensPerDay = 3,
            lastOpenedDate = "store-preview",
            lastRewardCoins = 150
        ),
        shopCoinReward = ShopCoinRewardState(
            claimedToday = 2,
            maxClaimsPerDay = 5,
            lastClaimDate = "store-preview",
            rewardCoins = 100
        ),
        oneMoreGameBonus = OneMoreGameBonusState(
            dateKey = "store-preview",
            gamesPlayedToday = 2,
            bonusClaimedToday = false
        ),
        dailyReward = DailyRewardState(
            streakDay = 14,
            dayInCycle = 7,
            rewardCoins = 500,
            nextRewardCoins = 75,
            rewardType = DailyRewardType.SuperBox,
            canClaim = true,
            isSuperReward = true,
            loyalBadgeUnlocked = true
        ),
        season = SeasonState(
            seasonNumber = 1,
            startDateKey = "store-preview",
            xp = 3_560,
            remainingDays = 18,
            claimedRewardLevels = (1..11).toSet(),
            preservedBadgeLevels = setOf(5, 10),
            xpBoostEndTimeMillis = System.currentTimeMillis() + 24 * 60 * 1_000L,
            missionDateKey = "store-preview",
            gamesPlayedToday = 2,
            rewardedAdsWatchedToday = 1,
            seasonXpEarnedToday = 45,
            claimedMissionIds = setOf("watch_1_rewarded_ad")
        ),
        achievements = storePreviewAchievements(),
        weeklyChallenge = ChallengeState(
            id = "weekly_classic_50_preview",
            type = WeeklyChallengeType.ClassicScore50,
            titleRes = R.string.weekly_challenge_classic_50_title,
            descriptionRes = R.string.weekly_challenge_classic_50_description,
            target = 50,
            progress = 41,
            completed = false,
            claimed = false,
            rewardCoins = 500,
            createdDate = "store-preview",
            remainingDays = 3
        ),
        dailyLeaderboardGoal = DailyLeaderboardGoalState(
            id = "daily_leaderboard_preview",
            type = DailyLeaderboardGoalType.ImproveScore10,
            titleRes = R.string.daily_leaderboard_goal_improve_score_title,
            descriptionRes = R.string.daily_leaderboard_goal_improve_score_description,
            target = 10,
            progress = 7,
            completed = false,
            claimed = false,
            rewardCoins = 250,
            createdDate = "store-preview",
            initialScore = 84,
            initialRank = 12
        ),
        personalGoal = PersonalGoalState(
            createdDate = "store-preview",
            targetScore = 94,
            initialBestScore = 84,
            progressScore = 90,
            completed = false,
            claimed = false,
            rewardCoins = 250
        ),
        comboChallenge = ComboChallengeState(
            createdDate = "store-preview",
            type = ComboChallengeType.Combo10,
            titleRes = R.string.combo_challenge_combo_10_title,
            descriptionRes = R.string.combo_challenge_combo_10_description,
            target = 10,
            progress = 7,
            gamesUsed = 0,
            completed = false,
            claimed = false,
            rewardCoins = 250
        ),
        latestUnlockedAchievementIds = listOf("combo_master"),
        lastLevelUp = null,
        firstTargetBonusClaimed = true,
        inviteRewardClaimed = true
    )
}

private fun storePreviewAchievements(): List<AchievementState> {
    return listOf(
        AchievementState("record_breaker", AchievementType.BreakRecord, AchievementCategory.Score, R.string.achievement_first_record_title, R.string.achievement_first_record_description, 1, 1, 75, 50, true, true),
        AchievementState("score_25", AchievementType.ScoreInSingleGame, AchievementCategory.Score, R.string.achievement_score_25_title, R.string.achievement_score_25_description, 25, 25, 100, 70, true, true),
        AchievementState("score_50", AchievementType.ScoreInSingleGame, AchievementCategory.Score, R.string.achievement_score_50_title, R.string.achievement_score_50_description, 50, 50, 180, 120, true, true),
        AchievementState("score_100", AchievementType.ScoreInSingleGame, AchievementCategory.Score, R.string.achievement_score_100_title, R.string.achievement_score_100_description, 100, 96, 350, 220, false, false),
        AchievementState("play_10", AchievementType.PlayGames, AchievementCategory.Game, R.string.achievement_play_10_title, R.string.achievement_play_10_description, 10, 10, 160, 100, true, true),
        AchievementState("play_50", AchievementType.PlayGames, AchievementCategory.Game, R.string.achievement_play_50_title, R.string.achievement_play_50_description, 50, 50, 350, 220, true, true),
        AchievementState("play_100", AchievementType.PlayGames, AchievementCategory.Game, R.string.achievement_play_100_title, R.string.achievement_play_100_description, 100, 100, 700, 420, true, false),
        AchievementState("combo_5", AchievementType.ReachCombo, AchievementCategory.Combo, R.string.achievement_combo_5_title, R.string.achievement_combo_5_description, 5, 5, 120, 80, true, true),
        AchievementState("combo_master", AchievementType.ReachCombo, AchievementCategory.Combo, R.string.achievement_combo_master_title, R.string.achievement_combo_master_description, 10, 10, 240, 150, true, false),
        AchievementState("combo_20", AchievementType.ReachCombo, AchievementCategory.Combo, R.string.achievement_combo_20_title, R.string.achievement_combo_20_description, 20, 20, 500, 300, true, true),
        AchievementState("rewarded_ad_1", AchievementType.RewardedAds, AchievementCategory.Ads, R.string.achievement_rewarded_ad_1_title, R.string.achievement_rewarded_ad_1_description, 1, 1, 75, 50, true, true),
        AchievementState("rewarded_ad_10", AchievementType.RewardedAds, AchievementCategory.Ads, R.string.achievement_rewarded_ad_10_title, R.string.achievement_rewarded_ad_10_description, 10, 10, 250, 160, true, true),
        AchievementState("rewarded_ad_50", AchievementType.RewardedAds, AchievementCategory.Ads, R.string.achievement_rewarded_ad_50_title, R.string.achievement_rewarded_ad_50_description, 50, 28, 1000, 600, false, false),
        AchievementState("theme_unlock_1", AchievementType.ThemesUnlocked, AchievementCategory.Theme, R.string.achievement_theme_unlock_1_title, R.string.achievement_theme_unlock_1_description, 1, 1, 150, 100, true, true),
        AchievementState("theme_unlock_5", AchievementType.ThemesUnlocked, AchievementCategory.Theme, R.string.achievement_theme_unlock_5_title, R.string.achievement_theme_unlock_5_description, 5, 5, 600, 360, true, false),
        AchievementState("theme_unlock_all", AchievementType.ThemesUnlocked, AchievementCategory.Theme, R.string.achievement_theme_unlock_all_title, R.string.achievement_theme_unlock_all_description, 10, 7, 1500, 900, false, false)
    )
}

private fun storePreviewLeaderboard(
    profile: PlayerProfile,
    progression: ProgressionState,
    bestScores: Map<GameMode, Int>
): LeaderboardSnapshot {
    return LeaderboardSnapshot(
        weekKey = "store-preview",
        selectedMode = GameMode.Classic,
        selectedPeriod = LeaderboardPeriod.AllTime,
        entries = listOf(
            LeaderboardEntry(1, "NeonAce", 108, PlayerTheme.GoldFire, RankTier.ReflexGod),
            LeaderboardEntry(2, profile.name, bestScores.getValue(GameMode.Classic), progression.activeTheme, RankTier.NeonMaster, isPlayer = true),
            LeaderboardEntry(3, "Pulse", 89, PlayerTheme.CyberBlue, RankTier.Platinum),
            LeaderboardEntry(4, "Blitz", 77, PlayerTheme.PurpleStorm, RankTier.Gold),
            LeaderboardEntry(5, "Nova", 69, PlayerTheme.IceNeon, RankTier.Gold),
            LeaderboardEntry(6, "Echo", 58, PlayerTheme.LavaCore, RankTier.Silver)
        ),
        playerRank = 2,
        motivationRes = R.string.leaderboard_motivation_top3,
        refreshedTick = 99,
        isLoading = false,
        isOffline = false,
        statusMessageRes = R.string.leaderboard_refreshed
    )
}
