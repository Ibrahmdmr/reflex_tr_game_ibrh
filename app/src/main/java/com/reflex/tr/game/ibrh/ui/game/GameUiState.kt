package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.R

const val MODE_MASTERY_XP_PER_LEVEL = 100
const val MODE_MASTERY_MAX_LEVEL = 10

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

@Immutable
data class DailyFeaturedModeState(
    val dateKey: String = "",
    val mode: GameMode = GameMode.Classic,
    val coinBonusPercent: Int = 20
)

enum class DailyChallenge(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val targetValue: Int,
    val rewardCoins: Int
) {
    ClassicScore20(
        titleRes = R.string.daily_challenge_classic_20_title,
        descriptionRes = R.string.daily_challenge_classic_20_description,
        targetValue = 20,
        rewardCoins = 100
    ),
    MovingTargetHits10(
        titleRes = R.string.daily_challenge_moving_10_title,
        descriptionRes = R.string.daily_challenge_moving_10_description,
        targetValue = 10,
        rewardCoins = 75
    ),
    FakeTargetScore5(
        titleRes = R.string.daily_challenge_fake_5_title,
        descriptionRes = R.string.daily_challenge_fake_5_description,
        targetValue = 5,
        rewardCoins = 50
    ),
    ColorReflexHits10(
        titleRes = R.string.daily_challenge_color_10_title,
        descriptionRes = R.string.daily_challenge_color_10_description,
        targetValue = 10,
        rewardCoins = 100
    ),
    Play3Games(
        titleRes = R.string.daily_challenge_play_3_title,
        descriptionRes = R.string.daily_challenge_play_3_description,
        targetValue = 3,
        rewardCoins = 50
    ),
    Combo5(
        titleRes = R.string.daily_challenge_combo_5_title,
        descriptionRes = R.string.daily_challenge_combo_5_description,
        targetValue = 5,
        rewardCoins = 75
    ),
    OpenLeaderboard(
        titleRes = R.string.daily_challenge_leaderboard_title,
        descriptionRes = R.string.daily_challenge_leaderboard_description,
        targetValue = 1,
        rewardCoins = 25
    ),
    VisitShop(
        titleRes = R.string.daily_challenge_shop_title,
        descriptionRes = R.string.daily_challenge_shop_description,
        targetValue = 1,
        rewardCoins = 25
    )
}

enum class RewardedAction {
    Continue,
    DoubleCoins,
    UnlockTheme,
    ProtectStreak,
    CoinChest,
    ShopCoinReward,
    DailyChallengeDoubleReward,
    Boost,
    SeasonXpBoost
}

enum class GameBoost(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val coinPrice: Int
) {
    ExtraTime(
        titleRes = R.string.boost_extra_time_title,
        descriptionRes = R.string.boost_extra_time_description,
        coinPrice = 120
    ),
    ExtraLife(
        titleRes = R.string.boost_extra_life_title,
        descriptionRes = R.string.boost_extra_life_description,
        coinPrice = 150
    ),
    ComboStart(
        titleRes = R.string.boost_combo_start_title,
        descriptionRes = R.string.boost_combo_start_description,
        coinPrice = 180
    )
}

enum class GamePowerUp(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val coinPrice: Int
) {
    ExtraTime(
        titleRes = R.string.power_up_extra_time_title,
        descriptionRes = R.string.power_up_extra_time_description,
        coinPrice = 250
    ),
    ExtraLife(
        titleRes = R.string.power_up_extra_life_title,
        descriptionRes = R.string.power_up_extra_life_description,
        coinPrice = 300
    ),
    ComboProtection(
        titleRes = R.string.power_up_combo_protection_title,
        descriptionRes = R.string.power_up_combo_protection_description,
        coinPrice = 500
    ),
    FirstMistakeForgiveness(
        titleRes = R.string.power_up_first_mistake_title,
        descriptionRes = R.string.power_up_first_mistake_description,
        coinPrice = 750
    )
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
const val FirstTargetBonusCoins = 50
const val FirstFiveExperienceGameLimit = 5
private const val OneMoreGameBonusOfferLimit = 3
const val SeasonDurationDays = 30
const val SeasonMaxLevel = 30
const val SeasonXpPerLevel = 200
const val SeasonXpBoostBonusPercent = 25
const val SeasonXpBoostDurationMillis = 30 * 60 * 1_000L

enum class SeasonRewardKind(@StringRes val titleRes: Int) {
    Coins(R.string.season_reward_coin),
    ThemeShard(R.string.season_reward_theme_shard),
    ProfileBadge(R.string.season_reward_profile_badge),
    BigCoinChest(R.string.season_reward_big_coin_chest),
    NeonAvatar(R.string.season_reward_neon_avatar),
    GoldFrame(R.string.season_reward_gold_frame),
    MatrixDiscount(R.string.season_reward_matrix_discount),
    LeaderboardBadge(R.string.season_reward_leaderboard_badge)
}

@Immutable
data class SeasonRewardState(
    val level: Int,
    val kind: SeasonRewardKind,
    val coinReward: Int,
    val premium: Boolean,
    val claimed: Boolean
)

enum class SeasonMissionType {
    PlayGames,
    WatchRewardedAd,
    EarnSeasonXp
}

@Immutable
data class SeasonMissionState(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val type: SeasonMissionType,
    val target: Int,
    val progress: Int,
    val rewardSeasonXp: Int,
    val claimed: Boolean
) {
    val completed: Boolean
        get() = progress >= target

    val progressPercent: Int
        get() = ((progress.coerceIn(0, target) * 100f) / target.coerceAtLeast(1)).toInt().coerceIn(0, 100)
}

@Immutable
data class SeasonQuestState(
    val type: SeasonQuestType,
    val progress: Int = 0,
    val claimed: Boolean = false,
    val rewardClaimedThisGame: Boolean = false
) {
    val target: Int
        get() = type.target

    val rewardCoins: Int
        get() = type.rewardCoins

    val completed: Boolean
        get() = progress >= target

    val progressPercent: Int
        get() = ((progress.coerceIn(0, target) * 100f) / target.coerceAtLeast(1)).toInt().coerceIn(0, 100)
}

enum class SeasonQuestType(
    @StringRes val titleRes: Int,
    val target: Int,
    val rewardCoins: Int
) {
    Play100Games(R.string.season_quest_play_100_title, 100, 750),
    Score3000(R.string.season_quest_score_3000_title, 3000, 1000),
    Combo10TwentyFiveTimes(R.string.season_quest_combo_10_title, 25, 900),
    Complete10DailyMissions(R.string.season_quest_daily_10_title, 10, 800),
    Use5Cosmetics(R.string.season_quest_cosmetic_5_title, 5, 700)
}

@Immutable
data class SeasonState(
    val seasonNumber: Int = 1,
    val startDateKey: String = "",
    val xp: Int = 0,
    val remainingDays: Int = SeasonDurationDays,
    val claimedRewardLevels: Set<Int> = emptySet(),
    val preservedBadgeLevels: Set<Int> = emptySet(),
    val xpBoostEndTimeMillis: Long = 0L,
    val missionDateKey: String = "",
    val gamesPlayedToday: Int = 0,
    val rewardedAdsWatchedToday: Int = 0,
    val seasonXpEarnedToday: Int = 0,
    val claimedMissionIds: Set<String> = emptySet(),
    val quests: List<SeasonQuestState> = SeasonQuestType.entries.map { SeasonQuestState(type = it) },
    val usedCosmeticKeys: Set<String> = emptySet()
) {
    val level: Int
        get() = (xp / SeasonXpPerLevel + 1).coerceIn(1, SeasonMaxLevel)

    val progressPercent: Int
        get() {
            if (level >= SeasonMaxLevel) return 100
            return (((xp % SeasonXpPerLevel) * 100f) / SeasonXpPerLevel).toInt().coerceIn(0, 100)
        }

    val nextReward: SeasonRewardState
        get() = seasonRewardForLevel(
            level = (1..SeasonMaxLevel).firstOrNull { it !in claimedRewardLevels } ?: SeasonMaxLevel,
            claimedLevels = claimedRewardLevels
        )

    val rewards: List<SeasonRewardState>
        get() = (1..SeasonMaxLevel).map { level ->
            seasonRewardForLevel(level = level, claimedLevels = claimedRewardLevels)
        }

    val isXpBoostActive: Boolean
        get() = xpBoostEndTimeMillis > System.currentTimeMillis()

    val xpBoostRemainingMinutes: Int
        get() = (((xpBoostEndTimeMillis - System.currentTimeMillis()).coerceAtLeast(0L) + 59_999L) / 60_000L)
            .toInt()

    val missions: List<SeasonMissionState>
        get() = listOf(
            SeasonMissionState(
                id = "play_3_games",
                titleRes = R.string.season_mission_play_3_title,
                descriptionRes = R.string.season_mission_play_3_description,
                type = SeasonMissionType.PlayGames,
                target = 3,
                progress = gamesPlayedToday,
                rewardSeasonXp = 90,
                claimed = "play_3_games" in claimedMissionIds
            ),
            SeasonMissionState(
                id = "watch_1_rewarded_ad",
                titleRes = R.string.season_mission_rewarded_1_title,
                descriptionRes = R.string.season_mission_rewarded_1_description,
                type = SeasonMissionType.WatchRewardedAd,
                target = 1,
                progress = rewardedAdsWatchedToday,
                rewardSeasonXp = 60,
                claimed = "watch_1_rewarded_ad" in claimedMissionIds
            ),
            SeasonMissionState(
                id = "earn_50_season_xp",
                titleRes = R.string.season_mission_xp_50_title,
                descriptionRes = R.string.season_mission_xp_50_description,
                type = SeasonMissionType.EarnSeasonXp,
                target = 50,
                progress = seasonXpEarnedToday,
                rewardSeasonXp = 80,
                claimed = "earn_50_season_xp" in claimedMissionIds
            )
        )

    val seasonQuestsCompleted: Boolean
        get() = quests.isNotEmpty() && quests.all { it.completed }

    val seasonQuestRewardCoinsThisGame: Int
        get() = quests.sumOf { if (it.rewardClaimedThisGame) it.rewardCoins else 0 }
}

fun seasonRewardForLevel(
    level: Int,
    claimedLevels: Set<Int> = emptySet()
): SeasonRewardState {
    val premiumKind = when (level) {
        5 -> SeasonRewardKind.NeonAvatar
        10 -> SeasonRewardKind.GoldFrame
        15 -> SeasonRewardKind.MatrixDiscount
        20 -> SeasonRewardKind.LeaderboardBadge
        25 -> SeasonRewardKind.NeonAvatar
        30 -> SeasonRewardKind.LeaderboardBadge
        else -> null
    }
    val kind = premiumKind ?: when {
        level % 6 == 0 -> SeasonRewardKind.BigCoinChest
        level % 3 == 0 -> SeasonRewardKind.ProfileBadge
        level % 2 == 0 -> SeasonRewardKind.ThemeShard
        else -> SeasonRewardKind.Coins
    }
    val coins = when (kind) {
        SeasonRewardKind.BigCoinChest -> 500 + level * 20
        SeasonRewardKind.Coins -> 100 + level * 15
        SeasonRewardKind.ThemeShard -> 75 + level * 10
        SeasonRewardKind.ProfileBadge -> 125 + level * 10
        SeasonRewardKind.NeonAvatar,
        SeasonRewardKind.GoldFrame,
        SeasonRewardKind.MatrixDiscount,
        SeasonRewardKind.LeaderboardBadge -> 250 + level * 20
    }
    return SeasonRewardState(
        level = level,
        kind = kind,
        coinReward = coins,
        premium = premiumKind != null,
        claimed = level in claimedLevels
    )
}

@Immutable
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

@Immutable
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

@Immutable
data class ShopCoinRewardState(
    val claimedToday: Int = 0,
    val maxClaimsPerDay: Int = 5,
    val lastClaimDate: String = "",
    val rewardCoins: Int = 100
) {
    val remainingClaims: Int
        get() = (maxClaimsPerDay - claimedToday).coerceAtLeast(0)

    val canClaim: Boolean
        get() = remainingClaims > 0
}

@Immutable
data class DailyRewardState(
    val streakDay: Int = 1,
    val dayInCycle: Int = 1,
    val rewardCoins: Int = defaultDailyRewardCoins(),
    val nextRewardCoins: Int = defaultDailyRewardCoins(),
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

private fun defaultDailyRewardCoins(): Int = DailyRewardCoinPlan.firstOrNull()?.coerceAtLeast(0) ?: 50

@Immutable
data class BonusHourState(
    val startHour: Int = 20,
    val endHour: Int = 21,
    val isActive: Boolean = false,
    val minutesUntilStart: Int = 0,
    val coinBonusPercent: Int = 25
)

@Immutable
data class DailyMiniTournamentState(
    val dateKey: String = "",
    val mode: GameMode = GameMode.Classic,
    val bestScore: Int = 0,
    val targetScore: Int = 25,
    val rewardCoins: Int = 150,
    val completed: Boolean = false,
    val claimed: Boolean = false,
    val rewardClaimedThisGame: Boolean = false
) {
    val remainingScore: Int
        get() = (targetScore - bestScore).coerceAtLeast(0)
}

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
    val personalRecords: PersonalRecordsState = PersonalRecordsState(),
    val selectedProfileBadgeIds: List<String> = emptyList(),
    val totalBossRoundHits: Int = 0,
    val totalUltraMomentHits: Int = 0,
    val seasonHunterBadgeUnlocked: Boolean = false,
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
data class PersonalRecordsState(
    val bestScore: Int = 0,
    val bestCombo: Int = 0,
    val bestAccuracyPercent: Int = 0,
    val longestSurvivalSeconds: Int = 0,
    val mostCoinsInGame: Int = 0
)

enum class ProfileBadge(
    val storageKey: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val lockedHintRes: Int,
    val rarityRank: Int
) {
    FirstGame("first_game", R.string.badge_first_game, R.string.badge_first_game_description, R.string.badge_first_game_hint, 1),
    ComboHunter("combo_hunter", R.string.badge_combo_hunter, R.string.badge_combo_hunter_description, R.string.badge_combo_hunter_hint, 2),
    RecordBreaker("record_breaker", R.string.badge_record_breaker, R.string.badge_record_breaker_description, R.string.badge_record_breaker_hint, 3),
    DailyPlayer("daily_player", R.string.badge_daily_player, R.string.badge_daily_player_description, R.string.badge_daily_player_hint, 2),
    LoyalPlayer("loyal_player", R.string.badge_loyal_player, R.string.badge_loyal_player_description, R.string.badge_loyal_player_hint, 4),
    CollectionMaster("collection_master", R.string.badge_collection_master, R.string.badge_collection_master_description, R.string.badge_collection_master_hint, 5),
    BossHunter("boss_hunter", R.string.badge_boss_hunter, R.string.badge_boss_hunter_description, R.string.badge_boss_hunter_hint, 4),
    UltraPlayer("ultra_player", R.string.badge_ultra_player, R.string.badge_ultra_player_description, R.string.badge_ultra_player_hint, 4),
    SeasonHunter("season_hunter", R.string.badge_season_hunter, R.string.badge_season_hunter_description, R.string.badge_season_hunter_hint, 5)
}

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
            ProfileBadge.ComboHunter -> progressionState.lifetimeMaxCombo >= 10
            ProfileBadge.RecordBreaker -> progressionState.personalRecords.bestScore > 0
            ProfileBadge.DailyPlayer -> progressionState.dailyReward.claimedToday || progressionState.dailyReward.streakDay > 1
            ProfileBadge.LoyalPlayer -> progressionState.dailyReward.loyalBadgeUnlocked
            ProfileBadge.CollectionMaster -> isProfileCollectionComplete(progressionState)
            ProfileBadge.BossHunter -> progressionState.totalBossRoundHits > 0
            ProfileBadge.UltraPlayer -> progressionState.totalUltraMomentHits > 0
            ProfileBadge.SeasonHunter -> progressionState.seasonHunterBadgeUnlocked
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

enum class PersonalRecordType(@StringRes val titleRes: Int) {
    HighestScore(R.string.personal_record_highest_score),
    HighestCombo(R.string.personal_record_highest_combo),
    BestAccuracy(R.string.personal_record_best_accuracy),
    LongestSurvival(R.string.personal_record_longest_survival),
    MostCoinsInGame(R.string.personal_record_most_coins),
    ClassicBest(R.string.personal_record_classic_best),
    MovingTargetBest(R.string.personal_record_moving_best),
    FakeTargetBest(R.string.personal_record_fake_best),
    ColorReflexBest(R.string.personal_record_color_best)
}

@Immutable
data class ModeMasteryProgress(
    val xp: Int = 0
) {
    val level: Int
        get() = ((xp.coerceAtLeast(0) / MODE_MASTERY_XP_PER_LEVEL) + 1)
            .coerceIn(1, MODE_MASTERY_MAX_LEVEL)

    val progressXp: Int
        get() = if (level >= MODE_MASTERY_MAX_LEVEL) {
            MODE_MASTERY_XP_PER_LEVEL
        } else {
            xp.coerceAtLeast(0) % MODE_MASTERY_XP_PER_LEVEL
        }

    val progressFraction: Float
        get() = (progressXp.toFloat() / MODE_MASTERY_XP_PER_LEVEL.toFloat()).coerceIn(0f, 1f)
}

@Immutable
data class ModeMasteryLevelUp(
    val mode: GameMode,
    val level: Int,
    val coinBonus: Int
)

fun modeMasteryProgressFor(
    modeMasteryXpByMode: Map<GameMode, Int>,
    mode: GameMode
): ModeMasteryProgress {
    return ModeMasteryProgress(modeMasteryXpByMode[mode]?.coerceAtLeast(0) ?: 0)
}

enum class TargetSkin(
    val storageKey: String,
    @StringRes val titleRes: Int,
    val coinPrice: Int
) {
    ClassicTarget(
        storageKey = "classic_target",
        titleRes = R.string.target_skin_classic,
        coinPrice = 0
    ),
    NeonRing(
        storageKey = "neon_ring",
        titleRes = R.string.target_skin_neon_ring,
        coinPrice = 750
    ),
    CyberDot(
        storageKey = "cyber_dot",
        titleRes = R.string.target_skin_cyber_dot,
        coinPrice = 1500
    ),
    FireCore(
        storageKey = "fire_core",
        titleRes = R.string.target_skin_fire_core,
        coinPrice = 3000
    ),
    MatrixOrb(
        storageKey = "matrix_orb",
        titleRes = R.string.target_skin_matrix_orb,
        coinPrice = 7500
    )
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

@Immutable
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

@Immutable
data class ChallengeState(
    val id: String,
    val type: WeeklyChallengeType = WeeklyChallengeType.ClassicScore50,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val target: Int,
    val progress: Int,
    val completed: Boolean,
    val claimed: Boolean = false,
    val rewardCoins: Int,
    val createdDate: String,
    val remainingDays: Int = 0
) {
    companion object {
        fun defaultWeekly(): ChallengeState {
            return ChallengeState(
                id = "weekly_classic_50",
                type = WeeklyChallengeType.ClassicScore50,
                titleRes = R.string.weekly_challenge_classic_50_title,
                descriptionRes = R.string.weekly_challenge_classic_50_description,
                target = 50,
                progress = 0,
                completed = false,
                claimed = false,
                rewardCoins = 500,
                createdDate = ""
            )
        }
    }
}

@Immutable
data class WeeklyGoalBoardState(
    val weekKey: String = "",
    val goals: List<WeeklyGoalState> = WeeklyGoalType.entries.map { WeeklyGoalState(type = it) },
    val bonusClaimed: Boolean = false,
    val bonusUnlockedThisGame: Boolean = false,
    val bonusRewardCoins: Int = 500
) {
    val allCompleted: Boolean
        get() = goals.isNotEmpty() && goals.all { it.completed }

    val totalRewardCoins: Int
        get() = goals.sumOf { if (it.rewardClaimedThisGame) it.rewardCoins else 0 } +
            if (bonusUnlockedThisGame) bonusRewardCoins else 0
}

@Immutable
data class WeeklyGoalState(
    val type: WeeklyGoalType,
    val progress: Int = 0,
    val claimed: Boolean = false,
    val rewardClaimedThisGame: Boolean = false
) {
    val target: Int
        get() = type.target

    val rewardCoins: Int
        get() = type.rewardCoins

    val completed: Boolean
        get() = progress >= target
}

enum class WeeklyGoalType(
    @StringRes val titleRes: Int,
    val target: Int,
    val rewardCoins: Int
) {
    Play20Games(R.string.weekly_goal_play_20_title, 20, 250),
    Score500(R.string.weekly_goal_score_500_title, 500, 500),
    Combo10FiveTimes(R.string.weekly_goal_combo_10_title, 5, 500)
}

enum class WeeklyChallengeType(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val target: Int,
    val rewardCoins: Int
) {
    ClassicScore50(
        titleRes = R.string.weekly_challenge_classic_50_title,
        descriptionRes = R.string.weekly_challenge_classic_50_description,
        target = 50,
        rewardCoins = 500
    ),
    ColorReflexScore30(
        titleRes = R.string.weekly_challenge_color_30_title,
        descriptionRes = R.string.weekly_challenge_color_30_description,
        target = 30,
        rewardCoins = 500
    ),
    FakeTargetScore20(
        titleRes = R.string.weekly_challenge_fake_20_title,
        descriptionRes = R.string.weekly_challenge_fake_20_description,
        target = 20,
        rewardCoins = 1000
    ),
    Play20Games(
        titleRes = R.string.weekly_challenge_play_20_title,
        descriptionRes = R.string.weekly_challenge_play_20_description,
        target = 20,
        rewardCoins = 250
    ),
    Combo10(
        titleRes = R.string.weekly_challenge_combo_10_title,
        descriptionRes = R.string.weekly_challenge_combo_10_description,
        target = 10,
        rewardCoins = 500
    )
}

@Immutable
data class DailyLeaderboardGoalState(
    val id: String = "",
    val type: DailyLeaderboardGoalType = DailyLeaderboardGoalType.SubmitScore,
    @StringRes val titleRes: Int = DailyLeaderboardGoalType.SubmitScore.titleRes,
    @StringRes val descriptionRes: Int = DailyLeaderboardGoalType.SubmitScore.descriptionRes,
    val target: Int = DailyLeaderboardGoalType.SubmitScore.target,
    val progress: Int = 0,
    val completed: Boolean = false,
    val claimed: Boolean = false,
    val rewardCoins: Int = DailyLeaderboardGoalType.SubmitScore.rewardCoins,
    val createdDate: String = "",
    val initialScore: Int = 0,
    val initialRank: Int = 0
)

@Immutable
data class PersonalGoalState(
    val createdDate: String = "",
    val targetScore: Int = 5,
    val initialBestScore: Int = 0,
    val progressScore: Int = 0,
    val completed: Boolean = false,
    val claimed: Boolean = false,
    val rewardCoins: Int = 100
) {
    val currentBestScore: Int
        get() = maxOf(initialBestScore, progressScore).coerceAtLeast(0)

    val remainingScore: Int
        get() = (targetScore - currentBestScore).coerceAtLeast(0)
}

@Immutable
data class ComboChallengeState(
    val createdDate: String = "",
    val type: ComboChallengeType = ComboChallengeType.Combo5,
    @StringRes val titleRes: Int = ComboChallengeType.Combo5.titleRes,
    @StringRes val descriptionRes: Int = ComboChallengeType.Combo5.descriptionRes,
    val target: Int = ComboChallengeType.Combo5.target,
    val progress: Int = 0,
    val gamesUsed: Int = 0,
    val completed: Boolean = false,
    val claimed: Boolean = false,
    val rewardCoins: Int = ComboChallengeType.Combo5.rewardCoins
)

enum class ComboChallengeType(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val target: Int,
    val rewardCoins: Int
) {
    Combo5(
        titleRes = R.string.combo_challenge_combo_5_title,
        descriptionRes = R.string.combo_challenge_combo_5_description,
        target = 5,
        rewardCoins = 100
    ),
    Combo10(
        titleRes = R.string.combo_challenge_combo_10_title,
        descriptionRes = R.string.combo_challenge_combo_10_description,
        target = 10,
        rewardCoins = 250
    ),
    TotalCombo20In3Games(
        titleRes = R.string.combo_challenge_total_20_title,
        descriptionRes = R.string.combo_challenge_total_20_description,
        target = 20,
        rewardCoins = 500
    ),
    NoMistake10Hits(
        titleRes = R.string.combo_challenge_no_mistake_10_title,
        descriptionRes = R.string.combo_challenge_no_mistake_10_description,
        target = 10,
        rewardCoins = 500
    )
}

enum class DailyLeaderboardGoalType(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val target: Int,
    val rewardCoins: Int
) {
    SubmitScore(
        titleRes = R.string.daily_leaderboard_goal_submit_title,
        descriptionRes = R.string.daily_leaderboard_goal_submit_description,
        target = 1,
        rewardCoins = 100
    ),
    ImproveScore10(
        titleRes = R.string.daily_leaderboard_goal_improve_score_title,
        descriptionRes = R.string.daily_leaderboard_goal_improve_score_description,
        target = 10,
        rewardCoins = 250
    ),
    Climb3Ranks(
        titleRes = R.string.daily_leaderboard_goal_climb_ranks_title,
        descriptionRes = R.string.daily_leaderboard_goal_climb_ranks_description,
        target = 3,
        rewardCoins = 500
    ),
    ReachTop50(
        titleRes = R.string.daily_leaderboard_goal_top_50_title,
        descriptionRes = R.string.daily_leaderboard_goal_top_50_description,
        target = 1,
        rewardCoins = 500
    )
}

@Immutable
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

@Immutable
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

@Immutable
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

@Immutable
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
                type = DailyChallenge.ClassicScore20,
                target = DailyChallenge.ClassicScore20.targetValue,
                progress = 0,
                completed = false,
                createdDate = "",
                rewardCoins = DailyChallenge.ClassicScore20.rewardCoins
            )
        }
    }
}

@Immutable
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

@Immutable
data class GameTarget(
    val id: Long,
    val position: TargetPosition,
    val role: GameTargetRole = GameTargetRole.Correct,
    val color: ReflexTargetColor = ReflexTargetColor.Red
)

enum class TimingGrade {
    Perfect,
    Great,
    Normal
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
data class PlayerProgressionUiState(
    val progressionState: ProgressionState = ProgressionState(),
    val playerProfile: PlayerProfile = PlayerProfile()
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
data class LeaderboardThemeState(
    val leaderboardSnapshot: LeaderboardSnapshot = LeaderboardSnapshot()
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
