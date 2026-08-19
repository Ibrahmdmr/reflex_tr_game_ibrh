package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.ads.PremiumState

internal fun showcasedProfileBadges(progressionState: ProgressionState): List<ProfileBadge> {
    val unlocked = unlockedProfileBadges(progressionState)
    val selected = progressionState.selectedProfileBadgeIds
        .mapNotNull { key -> ProfileBadge.entries.firstOrNull { it.storageKey == key } }
        .filter { it in unlocked }
        .distinct()
    if (selected.isNotEmpty()) {
        return (selected + automaticProfileBadges(unlocked)).distinct().take(3)
    }
    return automaticProfileBadges(unlocked).take(3)
}

internal fun unlockedProfileBadges(progressionState: ProgressionState): Set<ProfileBadge> {
    return ProfileBadge.entries.filterTo(mutableSetOf()) { badge ->
        when (badge) {
            ProfileBadge.FirstGame -> progressionState.totalGames > 0
            ProfileBadge.StarterComplete -> progressionState.starterJourney.isCompleted
            ProfileBadge.ComboHunter -> progressionState.lifetimeMaxCombo >= 10
            ProfileBadge.RecordBreaker -> progressionState.personalRecords.bestScore > 0
            ProfileBadge.DailyPlayer -> progressionState.dailyReward.claimedToday || progressionState.dailyReward.streakDay > 1
            ProfileBadge.LoyalPlayer -> progressionState.dailyReward.loyalBadgeUnlocked
            ProfileBadge.CollectionMaster -> isProfileCollectionComplete(progressionState)
            ProfileBadge.BossHunter -> progressionState.totalBossRoundHits > 0
            ProfileBadge.UltraPlayer -> progressionState.totalUltraMomentHits > 0
            ProfileBadge.SeasonHunter -> progressionState.seasonHunterBadgeUnlocked
            ProfileBadge.NeonLeaguePlayer -> progressionState.neonLeagueBadgeUnlocked
        }
    }
}

private fun automaticProfileBadges(unlocked: Set<ProfileBadge>): List<ProfileBadge> {
    val newest = ProfileBadge.entries.lastOrNull { it in unlocked }
    val rarest = unlocked.maxByOrNull { it.rarityRank }
    val active = listOf(
        ProfileBadge.RecordBreaker,
        ProfileBadge.ComboHunter,
        ProfileBadge.FirstGame
    ).firstOrNull { it in unlocked }
    return listOfNotNull(newest, rarest, active)
        .distinct()
        .ifEmpty { ProfileBadge.entries.take(3) }
}

private fun isProfileCollectionComplete(progressionState: ProgressionState): Boolean {
    return PlayerTheme.entries.all { it in progressionState.unlockedThemes } &&
        TargetSkin.entries.all { it in progressionState.unlockedTargetSkins }
}

@Immutable
data class GameRuntimeState(
    val score: Int = 0,
    val bestScore: Int = 0,
    val bestScoresByMode: Map<GameMode, Int> = GameMode.entries.associateWith { 0 },
    val isNewBestScore: Boolean = false,
    val difficultyLevel: Int = 1,
    val lives: Int = 3,
    val timeLeftSeconds: Int = 30,
    val targetPosition: TargetPosition = TargetPosition(),
    val targetSizeDp: Int = 82,
    val targetVisibleDurationMillis: Long = 1_800L,
    val targetLifetimeKey: Int = 0,
    val hasGameStarted: Boolean = false,
    val selectedMode: GameMode = GameMode.Classic,
    val dailyFeaturedMode: DailyFeaturedModeState = DailyFeaturedModeState(),
    val activeBoost: GameBoost? = null,
    val activePowerUp: GamePowerUp? = null,
    val isPowerUpConsumed: Boolean = false,
    val targets: List<GameTarget> = emptyList(),
    val activeColor: ReflexTargetColor = ReflexTargetColor.Red,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val perfectHits: Int = 0,
    val greatHits: Int = 0,
    val lastTimingGrade: TimingGrade? = null,
    val flawlessStreak: Int = 0,
    val maxFlawlessStreak: Int = 0,
    val flawlessStreakBonusCoins: Int = 0,
    val lastFlawlessStreakMilestone: Int? = null,
    val isBossRoundActive: Boolean = false,
    val bossRoundTimeLeftSeconds: Int = 0,
    val bossRoundHits: Int = 0,
    val bossRoundBonusCoins: Int = 0,
    val bossRoundTotalBonusCoins: Int = 0,
    val bossRoundResultHits: Int = 0,
    val bossRoundResultBonusCoins: Int = 0,
    val isBossRoundResultVisible: Boolean = false,
    val bossRoundFeedbackKey: Int = 0,
    val triggeredBossRoundScores: Set<Int> = emptySet(),
    val isUltraMomentActive: Boolean = false,
    val ultraMomentTimeLeftSeconds: Int = 0,
    val ultraMomentHits: Int = 0,
    val ultraMomentTotalHits: Int = 0,
    val ultraMomentBonusCoins: Int = 0,
    val ultraMomentTotalBonusCoins: Int = 0,
    val ultraMomentResultHits: Int = 0,
    val ultraMomentResultBonusCoins: Int = 0,
    val isUltraMomentResultVisible: Boolean = false,
    val ultraMomentFeedbackKey: Int = 0,
    val triggeredUltraMomentCombos: Set<Int> = emptySet(),
    val successfulHits: Int = 0,
    val totalAttempts: Int = 0,
    val newPersonalRecords: Set<PersonalRecordType> = emptySet(),
    val dailyChallengeState: DailyChallengeState = DailyChallengeState.default()
)

@Immutable
data class RewardAdState(
    val earnedCoinsThisGame: Int = 0,
    val baseCoinsThisGame: Int = 0,
    val isCoinDoubleClaimed: Boolean = false,
    val pendingRewardedAction: RewardedAction? = null,
    val oneMoreGameBonusEarnedThisGame: Int = 0,
    val hasUsedRewardContinue: Boolean = false,
    val isRewardContinueReady: Boolean = false,
    val canContinueWithReward: Boolean = false,
    val shouldRequestInterstitialAd: Boolean = false
)

@Immutable
data class DialogPopupState(
    val isPaused: Boolean = false,
    val isResumeGracePeriod: Boolean = false,
    val isGameOver: Boolean = false,
    val gameOverReason: String? = null,
    @StringRes val gameOverReasonRes: Int? = null,
    val shouldAutoShowDailyRewardDialog: Boolean = false,
    val shownModeTips: Set<GameMode> = emptySet(),
    val isStorePreviewMode: Boolean = false
)

@Immutable
data class GameUiState(
    val score: Int = 0,
    val bestScore: Int = 0,
    val bestScoresByMode: Map<GameMode, Int> = GameMode.entries.associateWith { 0 },
    val isNewBestScore: Boolean = false,
    val difficultyLevel: Int = 1,
    val lives: Int = 3,
    val timeLeftSeconds: Int = 30,
    val targetPosition: TargetPosition = TargetPosition(),
    val targetSizeDp: Int = 82,
    val targetVisibleDurationMillis: Long = 1_800L,
    val targetLifetimeKey: Int = 0,
    val hasGameStarted: Boolean = false,
    val selectedMode: GameMode = GameMode.Classic,
    val dailyFeaturedMode: DailyFeaturedModeState = DailyFeaturedModeState(),
    val activeBoost: GameBoost? = null,
    val activePowerUp: GamePowerUp? = null,
    val isPowerUpConsumed: Boolean = false,
    val targets: List<GameTarget> = emptyList(),
    val activeColor: ReflexTargetColor = ReflexTargetColor.Red,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val perfectHits: Int = 0,
    val greatHits: Int = 0,
    val lastTimingGrade: TimingGrade? = null,
    val flawlessStreak: Int = 0,
    val maxFlawlessStreak: Int = 0,
    val flawlessStreakBonusCoins: Int = 0,
    val lastFlawlessStreakMilestone: Int? = null,
    val isBossRoundActive: Boolean = false,
    val bossRoundTimeLeftSeconds: Int = 0,
    val bossRoundHits: Int = 0,
    val bossRoundBonusCoins: Int = 0,
    val bossRoundTotalBonusCoins: Int = 0,
    val bossRoundResultHits: Int = 0,
    val bossRoundResultBonusCoins: Int = 0,
    val isBossRoundResultVisible: Boolean = false,
    val bossRoundFeedbackKey: Int = 0,
    val triggeredBossRoundScores: Set<Int> = emptySet(),
    val isUltraMomentActive: Boolean = false,
    val ultraMomentTimeLeftSeconds: Int = 0,
    val ultraMomentHits: Int = 0,
    val ultraMomentTotalHits: Int = 0,
    val ultraMomentBonusCoins: Int = 0,
    val ultraMomentTotalBonusCoins: Int = 0,
    val ultraMomentResultHits: Int = 0,
    val ultraMomentResultBonusCoins: Int = 0,
    val isUltraMomentResultVisible: Boolean = false,
    val ultraMomentFeedbackKey: Int = 0,
    val triggeredUltraMomentCombos: Set<Int> = emptySet(),
    val successfulHits: Int = 0,
    val totalAttempts: Int = 0,
    val newPersonalRecords: Set<PersonalRecordType> = emptySet(),
    val dailyChallengeState: DailyChallengeState = DailyChallengeState.default(),
    val progressionState: ProgressionState = ProgressionState(),
    val playerProfile: PlayerProfile = PlayerProfile(),
    val leaderboardSnapshot: LeaderboardSnapshot = LeaderboardSnapshot(),
    val earnedCoinsThisGame: Int = 0,
    val leaguePointsEarnedThisGame: Int = 0,
    val leagueUpgradedTo: LeagueTier? = null,
    val rewardChestEarnedThisGame: RewardChestType? = null,
    val newPlayerTitlesThisGame: List<PlayerTitle> = emptyList(),
    val starterTaskCompletedThisGame: Boolean = false,
    val premiumState: PremiumState = PremiumState(),
    val baseCoinsThisGame: Int = 0,
    val isCoinDoubleClaimed: Boolean = false,
    val pendingRewardedAction: RewardedAction? = null,
    val oneMoreGameBonusEarnedThisGame: Int = 0,
    val isPaused: Boolean = false,
    val isResumeGracePeriod: Boolean = false,
    val isGameOver: Boolean = false,
    val gameOverReason: String? = null,
    @StringRes val gameOverReasonRes: Int? = null,
    val hasUsedRewardContinue: Boolean = false,
    val isRewardContinueReady: Boolean = false,
    val canContinueWithReward: Boolean = false,
    val shouldRequestInterstitialAd: Boolean = false,
    val shouldAutoShowDailyRewardDialog: Boolean = false,
    val shownModeTips: Set<GameMode> = emptySet(),
    val isStorePreviewMode: Boolean = false
) {
    val gameState: GameRuntimeState
        get() = GameRuntimeState(
            score = score,
            bestScore = bestScore,
            bestScoresByMode = bestScoresByMode,
            isNewBestScore = isNewBestScore,
            difficultyLevel = difficultyLevel,
            lives = lives,
            timeLeftSeconds = timeLeftSeconds,
            targetPosition = targetPosition,
            targetSizeDp = targetSizeDp,
            targetVisibleDurationMillis = targetVisibleDurationMillis,
            targetLifetimeKey = targetLifetimeKey,
            hasGameStarted = hasGameStarted,
            selectedMode = selectedMode,
            dailyFeaturedMode = dailyFeaturedMode,
            activeBoost = activeBoost,
            activePowerUp = activePowerUp,
            isPowerUpConsumed = isPowerUpConsumed,
            targets = targets,
            activeColor = activeColor,
            combo = combo,
            maxCombo = maxCombo,
            perfectHits = perfectHits,
            greatHits = greatHits,
            lastTimingGrade = lastTimingGrade,
            flawlessStreak = flawlessStreak,
            maxFlawlessStreak = maxFlawlessStreak,
            flawlessStreakBonusCoins = flawlessStreakBonusCoins,
            lastFlawlessStreakMilestone = lastFlawlessStreakMilestone,
            isBossRoundActive = isBossRoundActive,
            bossRoundTimeLeftSeconds = bossRoundTimeLeftSeconds,
            bossRoundHits = bossRoundHits,
            bossRoundBonusCoins = bossRoundBonusCoins,
            bossRoundTotalBonusCoins = bossRoundTotalBonusCoins,
            bossRoundResultHits = bossRoundResultHits,
            bossRoundResultBonusCoins = bossRoundResultBonusCoins,
            isBossRoundResultVisible = isBossRoundResultVisible,
            bossRoundFeedbackKey = bossRoundFeedbackKey,
            triggeredBossRoundScores = triggeredBossRoundScores,
            isUltraMomentActive = isUltraMomentActive,
            ultraMomentTimeLeftSeconds = ultraMomentTimeLeftSeconds,
            ultraMomentHits = ultraMomentHits,
            ultraMomentTotalHits = ultraMomentTotalHits,
            ultraMomentBonusCoins = ultraMomentBonusCoins,
            ultraMomentTotalBonusCoins = ultraMomentTotalBonusCoins,
            ultraMomentResultHits = ultraMomentResultHits,
            ultraMomentResultBonusCoins = ultraMomentResultBonusCoins,
            isUltraMomentResultVisible = isUltraMomentResultVisible,
            ultraMomentFeedbackKey = ultraMomentFeedbackKey,
            triggeredUltraMomentCombos = triggeredUltraMomentCombos,
            successfulHits = successfulHits,
            totalAttempts = totalAttempts,
            newPersonalRecords = newPersonalRecords,
            dailyChallengeState = dailyChallengeState
        )

    val playerProgressionState: PlayerProgressionUiState
        get() = PlayerProgressionUiState(
            progressionState = progressionState,
            playerProfile = playerProfile
        )

    val rewardAdState: RewardAdState
        get() = RewardAdState(
            earnedCoinsThisGame = earnedCoinsThisGame,
            baseCoinsThisGame = baseCoinsThisGame,
            isCoinDoubleClaimed = isCoinDoubleClaimed,
            pendingRewardedAction = pendingRewardedAction,
            oneMoreGameBonusEarnedThisGame = oneMoreGameBonusEarnedThisGame,
            hasUsedRewardContinue = hasUsedRewardContinue,
            isRewardContinueReady = isRewardContinueReady,
            canContinueWithReward = canContinueWithReward,
            shouldRequestInterstitialAd = shouldRequestInterstitialAd
        )

    val dialogPopupState: DialogPopupState
        get() = DialogPopupState(
            isPaused = isPaused,
            isResumeGracePeriod = isResumeGracePeriod,
            isGameOver = isGameOver,
            gameOverReason = gameOverReason,
            gameOverReasonRes = gameOverReasonRes,
            shouldAutoShowDailyRewardDialog = shouldAutoShowDailyRewardDialog,
            shownModeTips = shownModeTips,
            isStorePreviewMode = isStorePreviewMode
        )

    val leaderboardThemeState: LeaderboardThemeState
        get() = LeaderboardThemeState(
            leaderboardSnapshot = leaderboardSnapshot
        )
}
