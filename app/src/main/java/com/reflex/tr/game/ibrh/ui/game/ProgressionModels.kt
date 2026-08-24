package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.runtime.Immutable

internal const val XP_PER_LEVEL = 250

@Immutable
data class ProgressionState(
    val coins: Int = 0,
    val totalCoinsEarned: Int = 0,
    val totalCoinsSpent: Int = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val totalGames: Int = 0,
    val totalScore: Int = 0,
    val gamesPlayedByMode: Map<GameMode, Int> = GameMode.entries.associateWith { 0 },
    val modeMasteryXpByMode: Map<GameMode, Int> = GameMode.entries.associateWith { 0 },
    val totalHits: Int = 0,
    val totalMisses: Int = 0,
    val lifetimeMaxCombo: Int = 0,
    val lifetimeMaxFlawlessStreak: Int = 0,
    val rewardedAdWatchCount: Int = 0,
    val selectedTheme: PlayerTheme = PlayerTheme.NeonRed,
    val unlockedThemes: Set<PlayerTheme> = setOf(PlayerTheme.NeonRed),
    val selectedTargetSkin: TargetSkin = TargetSkin.ClassicTarget,
    val unlockedTargetSkins: Set<TargetSkin> = setOf(TargetSkin.ClassicTarget),
    val trialTheme: PlayerTheme? = null,
    val trialGamesRemaining: Int = 0,
    val coinChest: CoinChestState = CoinChestState(),
    val rewardChest: RewardChestState = RewardChestState(),
    val starterJourney: StarterJourneyState = StarterJourneyState(),
    val shopCoinReward: ShopCoinRewardState = ShopCoinRewardState(),
    val oneMoreGameBonus: OneMoreGameBonusState = OneMoreGameBonusState(),
    val dailyReward: DailyRewardState = DailyRewardState(),
    val bonusHour: BonusHourState = BonusHourState(),
    val dailyMiniTournament: DailyMiniTournamentState = DailyMiniTournamentState(),
    val season: SeasonState = SeasonState(),
    val achievements: List<AchievementState> = emptyList(),
    val weeklyChallenge: ChallengeState = ChallengeState.defaultWeekly(),
    val weeklyGoalBoard: WeeklyGoalBoardState = WeeklyGoalBoardState(),
    val dailyLeaderboardGoal: DailyLeaderboardGoalState = DailyLeaderboardGoalState(),
    val personalGoal: PersonalGoalState = PersonalGoalState(),
    val comboChallenge: ComboChallengeState = ComboChallengeState(),
    val dailyEvent: DailyEventState = DailyEventState(),
    val weeklyLeague: WeeklyLeagueState = WeeklyLeagueState(),
    val personalRecords: PersonalRecordsState = PersonalRecordsState(),
    val selectedProfileBadgeIds: List<String> = emptyList(),
    val totalBossRoundHits: Int = 0,
    val totalUltraMomentHits: Int = 0,
    val seasonHunterBadgeUnlocked: Boolean = false,
    val neonLeagueBadgeUnlocked: Boolean = false,
    val lastModeMasteryLevelUp: ModeMasteryLevelUp? = null,
    val latestUnlockedAchievementIds: List<String> = emptyList(),
    val latestUnlockedProfileBadges: Set<ProfileBadge> = emptySet(),
    val lastLevelUp: Int? = null,
    val firstTargetBonusClaimed: Boolean = false,
    val inviteRewardClaimed: Boolean = false
) {
    val activeTheme: PlayerTheme
        get() = trialTheme ?: selectedTheme
}

@Immutable
data class PlayerProgressionUiState(
    val progressionState: ProgressionState = ProgressionState(),
    val playerProfile: PlayerProfile = PlayerProfile()
)
