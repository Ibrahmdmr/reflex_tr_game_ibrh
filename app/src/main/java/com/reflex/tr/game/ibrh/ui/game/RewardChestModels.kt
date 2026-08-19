package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.R

/**
 * Chest tiers, weakest first.
 *
 * [rollWeightPercent] is the chance of drawing this tier; the weights add up to 100 and
 * [rollRewardChestType] derives its ranges from them, so the odds live in this table alone.
 */
enum class RewardChestType(
    val storageKey: String,
    @StringRes val titleRes: Int,
    val minCoins: Int,
    val maxCoins: Int,
    val seasonXpChancePercent: Int,
    val seasonXpReward: Int,
    val rollWeightPercent: Int
) {
    Small(
        storageKey = "small",
        titleRes = R.string.reward_chest_type_small,
        minCoins = 25,
        maxCoins = 75,
        seasonXpChancePercent = 0,
        seasonXpReward = 0,
        rollWeightPercent = 70
    ),
    Neon(
        storageKey = "neon",
        titleRes = R.string.reward_chest_type_neon,
        minCoins = 75,
        maxCoins = 150,
        seasonXpChancePercent = 25,
        seasonXpReward = 40,
        rollWeightPercent = 25
    ),
    Legendary(
        storageKey = "legendary",
        titleRes = R.string.reward_chest_type_legendary,
        minCoins = 150,
        maxCoins = 300,
        seasonXpChancePercent = 60,
        seasonXpReward = 90,
        rollWeightPercent = 5
    );

    companion object {
        fun fromStorageKey(key: String): RewardChestType? =
            entries.firstOrNull { it.storageKey == key }
    }
}

/** Why a chest was handed out. Reported to analytics; it never changes what the chest pays. */
enum class RewardChestSource(val storageKey: String) {
    GameCount("game_count"),
    DailyEvent("daily_event"),
    WeeklyGoal("weekly_goal"),
    NewRecord("new_record")
}

/** Chests waiting to be opened, and how close the next "every N games" chest is. */
@Immutable
data class RewardChestState(
    val pendingChests: List<RewardChestType> = emptyList(),
    val gamesSinceLastChest: Int = 0,
    val lastRewardCoins: Int = 0,
    val lastRewardSeasonXp: Int = 0
) {
    val pendingCount: Int
        get() = pendingChests.size

    val hasPendingChest: Boolean
        get() = pendingChests.isNotEmpty()

    /** Opened first and named on the cards, so the chest promised is the one that pays out. */
    val bestPendingChest: RewardChestType?
        get() = pendingChests.maxByOrNull { it.ordinal }

    val gamesUntilNextChest: Int
        get() = (GAMES_PER_REWARD_CHEST - gamesSinceLastChest).coerceIn(0, GAMES_PER_REWARD_CHEST)
}

/** What one opened chest paid. [seasonXp] is 0 when the tier rolled no XP. */
@Immutable
data class RewardChestReward(
    val type: RewardChestType,
    val coins: Int,
    val seasonXp: Int = 0
)
