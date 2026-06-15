package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import com.reflex.tr.game.ibrh.R

enum class GameMode(
    val storageKey: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val longDescriptionRes: Int,
    @StringRes val difficultyRes: Int,
    @StringRes val arenaTitleRes: Int,
    @StringRes val startButtonRes: Int
) {
    Classic(
        storageKey = "classic",
        titleRes = R.string.mode_classic_title,
        descriptionRes = R.string.mode_classic_description,
        longDescriptionRes = R.string.mode_classic_long_description,
        difficultyRes = R.string.mode_difficulty_easy,
        arenaTitleRes = R.string.arena_reflex,
        startButtonRes = R.string.start_classic_game
    ),
    MovingTarget(
        storageKey = "moving_target",
        titleRes = R.string.mode_moving_title,
        descriptionRes = R.string.mode_moving_description,
        longDescriptionRes = R.string.mode_moving_long_description,
        difficultyRes = R.string.mode_difficulty_medium,
        arenaTitleRes = R.string.arena_target,
        startButtonRes = R.string.start_moving_game
    ),
    FakeTarget(
        storageKey = "fake_target",
        titleRes = R.string.mode_fake_title,
        descriptionRes = R.string.mode_fake_description,
        longDescriptionRes = R.string.mode_fake_long_description,
        difficultyRes = R.string.mode_difficulty_hard,
        arenaTitleRes = R.string.arena_challenge,
        startButtonRes = R.string.start_fake_game
    ),
    ColorReflex(
        storageKey = "color_reflex",
        titleRes = R.string.mode_color_title,
        descriptionRes = R.string.mode_color_description,
        longDescriptionRes = R.string.mode_color_long_description,
        difficultyRes = R.string.mode_difficulty_medium,
        arenaTitleRes = R.string.arena_challenge,
        startButtonRes = R.string.start_color_game
    )
}

enum class DailyChallenge(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val targetValue: Int
) {
    Score20(
        titleRes = R.string.daily_challenge_score_20_title,
        descriptionRes = R.string.daily_challenge_score_20_description,
        targetValue = 20
    ),
    Play3Games(
        titleRes = R.string.daily_challenge_play_3_title,
        descriptionRes = R.string.daily_challenge_play_3_description,
        targetValue = 3
    ),
    FakeTarget10(
        titleRes = R.string.daily_challenge_fake_10_title,
        descriptionRes = R.string.daily_challenge_fake_10_description,
        targetValue = 10
    )
}

enum class RewardedAction {
    Continue,
    DoubleCoins,
    UnlockTheme,
    ProtectStreak,
    CoinChest,
    DailyChallengeDoubleReward
}

enum class ThemeRarity(@StringRes val titleRes: Int) {
    Common(R.string.theme_rarity_common),
    Rare(R.string.theme_rarity_rare),
    Epic(R.string.theme_rarity_epic),
    Legendary(R.string.theme_rarity_legendary),
    Mythic(R.string.theme_rarity_mythic)
}

enum class PlayerTheme(
    val storageKey: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val coinPrice: Int,
    val rarity: ThemeRarity
) {
    NeonRed(
        storageKey = "neon_red",
        titleRes = R.string.theme_neon_red,
        descriptionRes = R.string.theme_neon_red_description,
        coinPrice = 0,
        rarity = ThemeRarity.Common
    ),
    CyberBlue(
        storageKey = "cyber_blue",
        titleRes = R.string.theme_cyber_blue,
        descriptionRes = R.string.theme_cyber_blue_description,
        coinPrice = 600,
        rarity = ThemeRarity.Rare
    ),
    PurpleStorm(
        storageKey = "purple_storm",
        titleRes = R.string.theme_purple_storm,
        descriptionRes = R.string.theme_purple_storm_description,
        coinPrice = 1500,
        rarity = ThemeRarity.Rare
    ),
    IceNeon(
        storageKey = "ice_neon",
        titleRes = R.string.theme_ice_neon,
        descriptionRes = R.string.theme_ice_neon_description,
        coinPrice = 3500,
        rarity = ThemeRarity.Epic
    ),
    LavaCore(
        storageKey = "lava_core",
        titleRes = R.string.theme_lava_core,
        descriptionRes = R.string.theme_lava_core_description,
        coinPrice = 6500,
        rarity = ThemeRarity.Epic
    ),
    ToxicPulse(
        storageKey = "toxic_pulse",
        titleRes = R.string.theme_toxic_pulse,
        descriptionRes = R.string.theme_toxic_pulse_description,
        coinPrice = 10000,
        rarity = ThemeRarity.Epic
    ),
    MatrixGreen(
        storageKey = "matrix_green",
        titleRes = R.string.theme_matrix_green,
        descriptionRes = R.string.theme_matrix_green_description,
        coinPrice = 90000,
        rarity = ThemeRarity.Mythic
    ),
    GoldFire(
        storageKey = "gold_fire",
        titleRes = R.string.theme_gold_fire,
        descriptionRes = R.string.theme_gold_fire_description,
        coinPrice = 18000,
        rarity = ThemeRarity.Mythic
    ),
    ShadowBlack(
        storageKey = "shadow_black",
        titleRes = R.string.theme_shadow_black,
        descriptionRes = R.string.theme_shadow_black_description,
        coinPrice = 26000,
        rarity = ThemeRarity.Legendary
    ),
    GalaxyWave(
        storageKey = "galaxy_wave",
        titleRes = R.string.theme_galaxy_wave,
        descriptionRes = R.string.theme_galaxy_wave_description,
        coinPrice = 42000,
        rarity = ThemeRarity.Mythic
    ),
    RainbowFlux(
        storageKey = "rainbow_flux",
        titleRes = R.string.theme_rainbow_flux,
        descriptionRes = R.string.theme_rainbow_flux_description,
        coinPrice = 60000,
        rarity = ThemeRarity.Legendary
    )
}

enum class DailyRewardType {
    Coins,
    SuperBox
}

val DailyRewardCoinPlan = listOf(50, 75, 100, 150, 200, 300, 500)
val CoinChestRewardPlan = listOf(50, 75, 100, 150, 250)
const val OneMoreGameBonusCoins = 25
private const val OneMoreGameBonusOfferLimit = 3

data class OneMoreGameBonusState(
    val dateKey: String = "",
    val gamesPlayedToday: Int = 0,
    val bonusClaimedToday: Boolean = false,
    val rewardCoins: Int = OneMoreGameBonusCoins
) {
    val shouldShowGameOverOffer: Boolean
        get() = !bonusClaimedToday && gamesPlayedToday in 1 until OneMoreGameBonusOfferLimit

    val shouldRewardNextCompletedGame: Boolean
        get() = shouldShowGameOverOffer
}

data class CoinChestState(
    val openedToday: Int = 0,
    val maxOpensPerDay: Int = 3,
    val lastOpenedDate: String = "",
    val lastRewardCoins: Int = 0
) {
    val remainingOpens: Int
        get() = (maxOpensPerDay - openedToday).coerceAtLeast(0)

    val canOpen: Boolean
        get() = remainingOpens > 0
}

data class DailyRewardState(
    val streakDay: Int = 1,
    val dayInCycle: Int = 1,
    val rewardCoins: Int = DailyRewardCoinPlan.first(),
    val nextRewardCoins: Int = DailyRewardCoinPlan.first(),
    val rewardType: DailyRewardType = DailyRewardType.Coins,
    val rewardTheme: PlayerTheme? = null,
    val canClaim: Boolean = false,
    val canProtectStreak: Boolean = false,
    val isStreakAtRisk: Boolean = false,
    val isSuperReward: Boolean = false,
    val loyalBadgeUnlocked: Boolean = false,
    val claimedToday: Boolean = false,
    val lastClaimDate: String = ""
)

data class ProgressionState(
    val coins: Int = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val totalGames: Int = 0,
    val totalHits: Int = 0,
    val lifetimeMaxCombo: Int = 0,
    val rewardedAdWatchCount: Int = 0,
    val selectedTheme: PlayerTheme = PlayerTheme.NeonRed,
    val unlockedThemes: Set<PlayerTheme> = setOf(PlayerTheme.NeonRed),
    val trialTheme: PlayerTheme? = null,
    val trialGamesRemaining: Int = 0,
    val coinChest: CoinChestState = CoinChestState(),
    val oneMoreGameBonus: OneMoreGameBonusState = OneMoreGameBonusState(),
    val dailyReward: DailyRewardState = DailyRewardState(),
    val achievements: List<AchievementState> = emptyList(),
    val weeklyChallenge: ChallengeState = ChallengeState.defaultWeekly(),
    val latestUnlockedAchievementIds: List<String> = emptyList(),
    val lastLevelUp: Int? = null
) {
    val activeTheme: PlayerTheme
        get() = trialTheme ?: selectedTheme
}

enum class AchievementType {
    BreakRecord,
    ScoreInSingleGame,
    PlayGames,
    ReachCombo,
    RewardedAds,
    ThemesUnlocked
}

enum class AchievementCategory(@StringRes val titleRes: Int) {
    Score(R.string.achievement_category_score),
    Game(R.string.achievement_category_game),
    Combo(R.string.achievement_category_combo),
    Ads(R.string.achievement_category_ads),
    Theme(R.string.achievement_category_theme)
}

data class AchievementState(
    val id: String,
    val type: AchievementType,
    val category: AchievementCategory,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val target: Int,
    val progress: Int,
    val rewardCoins: Int,
    val rewardXp: Int,
    val unlocked: Boolean,
    val claimed: Boolean
) {
    val progressPercent: Int
        get() = ((progress.coerceAtMost(target) * 100f) / target.coerceAtLeast(1)).toInt()
}

data class ChallengeState(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val target: Int,
    val progress: Int,
    val completed: Boolean,
    val rewardCoins: Int,
    val createdDate: String
) {
    companion object {
        fun defaultWeekly(): ChallengeState {
            return ChallengeState(
                id = "weekly_score_100",
                titleRes = R.string.weekly_challenge_title_value,
                descriptionRes = R.string.weekly_challenge_description,
                target = 100,
                progress = 0,
                completed = false,
                rewardCoins = 300,
                createdDate = ""
            )
        }
    }
}

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val score: Int,
    val theme: PlayerTheme = PlayerTheme.NeonRed,
    val rankTier: RankTier = RankTier.Bronze,
    val isPlayer: Boolean = false
)

enum class PlayerTitle(@StringRes val titleRes: Int) {
    ReflexHunter(R.string.player_title_reflex_hunter),
    ComboMaster(R.string.player_title_combo_master),
    NeonLegend(R.string.player_title_neon_legend),
    TargetKing(R.string.player_title_target_king)
}

enum class RankTier(@StringRes val titleRes: Int) {
    Bronze(R.string.rank_bronze),
    Silver(R.string.rank_silver),
    Gold(R.string.rank_gold),
    Platinum(R.string.rank_platinum),
    NeonMaster(R.string.rank_neon_master),
    ReflexGod(R.string.rank_reflex_god)
}

data class PlayerProfile(
    val name: String = "",
    val title: PlayerTitle = PlayerTitle.ReflexHunter,
    val weeklyBestScore: Int = 0,
    val weeklyBestScoresByMode: Map<GameMode, Int> = GameMode.entries.associateWith { 0 },
    val hasCompletedNamePrompt: Boolean = false
) {
    val hasName: Boolean
        get() = name.isNotBlank()
}

enum class LeaderboardPeriod {
    Weekly,
    AllTime
}

data class LeaderboardSnapshot(
    val weekKey: String = "",
    val selectedMode: GameMode = GameMode.Classic,
    val selectedPeriod: LeaderboardPeriod = LeaderboardPeriod.AllTime,
    val entries: List<LeaderboardEntry> = emptyList(),
    val playerRank: Int = 0,
    @StringRes val motivationRes: Int = R.string.leaderboard_motivation_default,
    val motivationPlayerName: String = "",
    val motivationScoreGap: Int = 0,
    val refreshedTick: Int = 0,
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    @StringRes val statusMessageRes: Int? = null
)

data class DailyChallengeState(
    val id: String,
    val type: DailyChallenge,
    val target: Int,
    val progress: Int,
    val completed: Boolean,
    val createdDate: String,
    val rewardCoins: Int = 100,
    val rewardClaimed: Boolean = false,
    val doubleRewardClaimed: Boolean = false
) {
    companion object {
        fun default(): DailyChallengeState {
            return DailyChallengeState(
                id = "default_score20",
                type = DailyChallenge.Score20,
                target = DailyChallenge.Score20.targetValue,
                progress = 0,
                completed = false,
                createdDate = "",
                rewardCoins = 100
            )
        }
    }
}

data class TargetPosition(
    val xFraction: Float = 0.5f,
    val yFraction: Float = 0.5f
)

enum class ReflexTargetColor(@StringRes val labelRes: Int) {
    Red(R.string.target_color_red),
    Blue(R.string.target_color_blue),
    Gold(R.string.target_color_yellow),
    Teal(R.string.target_color_green)
}

enum class GameTargetRole {
    Correct,
    Fake,
    WrongColor
}

data class GameTarget(
    val id: Long,
    val position: TargetPosition,
    val role: GameTargetRole = GameTargetRole.Correct,
    val color: ReflexTargetColor = ReflexTargetColor.Red
)

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
    val targets: List<GameTarget> = emptyList(),
    val activeColor: ReflexTargetColor = ReflexTargetColor.Red,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val successfulHits: Int = 0,
    val totalAttempts: Int = 0,
    val dailyChallengeState: DailyChallengeState = DailyChallengeState.default(),
    val progressionState: ProgressionState = ProgressionState(),
    val playerProfile: PlayerProfile = PlayerProfile(),
    val leaderboardSnapshot: LeaderboardSnapshot = LeaderboardSnapshot(),
    val earnedCoinsThisGame: Int = 0,
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
    val shouldRequestInterstitialAd: Boolean = false
)
