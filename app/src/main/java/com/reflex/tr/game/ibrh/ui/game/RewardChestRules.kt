package com.reflex.tr.game.ibrh.ui.game

import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseParam
import kotlin.random.Random

internal const val GAMES_PER_REWARD_CHEST = 5

internal const val MAX_PENDING_REWARD_CHESTS = 5

private const val NEW_RECORD_CHEST_CHANCE_PERCENT = 20

private const val REWARD_CHEST_COIN_STEP = 5

data class RewardChestEarn(
    val state: RewardChestState,
    val earnedChest: RewardChestType?,
    val source: RewardChestSource?
)

data class RewardChestOpen(
    val state: RewardChestState,
    val reward: RewardChestReward
)

/** Leftover probability falls to the weakest tier, so a mistuned table still returns a chest. */
internal fun rollRewardChestType(random: Random = Random): RewardChestType {
    val roll = random.nextInt(100)
    var threshold = 0
    RewardChestType.entries.forEach { type ->
        threshold += type.rollWeightPercent.coerceAtLeast(0)
        if (roll < threshold) return type
    }
    return RewardChestType.Small
}

internal fun rollRewardChestReward(
    type: RewardChestType,
    random: Random = Random
): RewardChestReward {
    val minCoins = type.minCoins.coerceAtLeast(0)
    val maxCoins = type.maxCoins.coerceAtLeast(minCoins)
    val drawnCoins = if (maxCoins > minCoins) {
        random.nextInt(minCoins, maxCoins + 1)
    } else {
        minCoins
    }
    val coins = ((drawnCoins / REWARD_CHEST_COIN_STEP) * REWARD_CHEST_COIN_STEP)
        .coerceAtLeast(minCoins)
    val seasonXp = if (
        type.seasonXpReward > 0 && random.nextInt(100) < type.seasonXpChancePercent
    ) {
        type.seasonXpReward
    } else {
        0
    }
    return RewardChestReward(type = type, coins = coins, seasonXp = seasonXp)
}

/** At most one chest per run: sources are checked in priority order and the first match wins. */
internal fun earnRewardChestAfterGame(
    state: RewardChestState,
    dailyEventJustCompleted: Boolean,
    weeklyGoalJustCompleted: Boolean,
    isNewBestScore: Boolean,
    random: Random = Random
): RewardChestEarn {
    val playedGames = (state.gamesSinceLastChest.coerceAtLeast(0) + 1)
        .coerceAtMost(GAMES_PER_REWARD_CHEST)
    val played = state.copy(gamesSinceLastChest = playedGames)
    // A full stack still counts the run, so the chest arrives as soon as one is opened.
    if (played.pendingCount >= MAX_PENDING_REWARD_CHESTS) {
        return RewardChestEarn(played, earnedChest = null, source = null)
    }

    val source = when {
        playedGames >= GAMES_PER_REWARD_CHEST -> RewardChestSource.GameCount
        dailyEventJustCompleted -> RewardChestSource.DailyEvent
        weeklyGoalJustCompleted -> RewardChestSource.WeeklyGoal
        isNewBestScore && random.nextInt(100) < NEW_RECORD_CHEST_CHANCE_PERCENT ->
            RewardChestSource.NewRecord
        else -> null
    } ?: return RewardChestEarn(played, earnedChest = null, source = null)

    // The guaranteed every-N-games chest is always the small one; bonus sources roll a tier.
    val chest = if (source == RewardChestSource.GameCount) {
        RewardChestType.Small
    } else {
        rollRewardChestType(random)
    }
    return RewardChestEarn(
        state = grantedRewardChest(played, chest).copy(
            // Only the every-N-games chest restarts the count, so bonus chests cannot delay it.
            gamesSinceLastChest = if (source == RewardChestSource.GameCount) 0 else playedGames
        ),
        earnedChest = chest,
        source = source
    )
}

/** Hands out a chest from outside a run. Returns [state] unchanged when the stack is full. */
internal fun grantedRewardChest(
    state: RewardChestState,
    chest: RewardChestType
): RewardChestState {
    if (state.pendingCount >= MAX_PENDING_REWARD_CHESTS) return state
    return state.copy(pendingChests = state.pendingChests + chest)
}

internal fun openBestRewardChest(
    state: RewardChestState,
    random: Random = Random
): RewardChestOpen? {
    val chest = state.bestPendingChest ?: return null
    val reward = rollRewardChestReward(chest, random)
    val remaining = state.pendingChests.toMutableList().apply { remove(chest) }
    return RewardChestOpen(
        state = state.copy(
            pendingChests = remaining,
            lastRewardCoins = reward.coins,
            lastRewardSeasonXp = reward.seasonXp
        ),
        reward = reward
    )
}

/** Carries only the chest figures — never playerName or uid. */
internal fun logRewardChestEvent(
    event: FirebaseEvent,
    type: RewardChestType,
    source: RewardChestSource? = null,
    rewardCoins: Int = 0,
    rewardSeasonXp: Int = 0
) {
    logGameEvent(event) {
        putString(FirebaseParam.ChestType.key, type.storageKey)
        source?.let { putString(FirebaseParam.Source.key, it.storageKey) }
        putInt(FirebaseParam.RewardCoin.key, rewardCoins.coerceAtLeast(0))
        putInt(FirebaseParam.RewardXp.key, rewardSeasonXp.coerceAtLeast(0))
    }
}
