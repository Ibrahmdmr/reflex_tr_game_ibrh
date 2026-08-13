package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.R

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
