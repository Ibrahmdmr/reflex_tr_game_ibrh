package com.reflex.tr.game.ibrh.ui.game

import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseParam
import kotlin.random.Random

/** Every rule the reward chest needs. UI reads state; only these functions decide it. */

/** One guaranteed chest per this many finished runs. */
internal const val GAMES_PER_REWARD_CHEST = 5

/** Ceiling on unopened chests, so a long break cannot pile up a stack of them. */
internal const val MAX_PENDING_REWARD_CHESTS = 5

/** A new record only sometimes pays a chest; otherwise every strong run would. */
private const val NEW_RECORD_CHEST_CHANCE_PERCENT = 20

/** Coin payouts land on this step, so a reward reads as a round number. */
private const val REWARD_CHEST_COIN_STEP = 5

/** What one finished run did to the chest state. */
data class RewardChestEarn(
    val state: RewardChestState,
    val earnedChest: RewardChestType?,
    val source: RewardChestSource?
)

/** An opened chest: the state with that chest removed, and what it paid. */
data class RewardChestOpen(
    val state: RewardChestState,
    val reward: RewardChestReward
)

/**
 * Draws a tier from [RewardChestType.rollWeightPercent]. Any probability the weights leave over
 * falls to the first (weakest) tier, so a mistuned table can never fail to return a chest.
 */
internal fun rollRewardChestType(random: Random = Random): RewardChestType {
    val roll = random.nextInt(100)
    var threshold = 0
    RewardChestType.entries.forEach { type ->
        threshold += type.rollWeightPercent.coerceAtLeast(0)
        if (roll < threshold) return type
    }
    return RewardChestType.Small
}

/** The payout for one chest of [type]. Never negative, and never below the tier's floor. */
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

/**
 * Applies one finished run. At most one chest per run: the sources are checked in priority order
 * and the first match wins, so a run that satisfies every condition still pays exactly one.
 */
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

/**
 * Hands out one chest from outside a run — a starter-journey day, for instance. Returns the state
 * unchanged when the stack is already full, so no caller has to know about the cap.
 */
internal fun grantedRewardChest(
    state: RewardChestState,
    chest: RewardChestType
): RewardChestState {
    if (state.pendingCount >= MAX_PENDING_REWARD_CHESTS) return state
    return state.copy(pendingChests = state.pendingChests + chest)
}

/** Opens the best waiting chest, or returns null when there is nothing to open. */
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

/**
 * Reward-chest analytics. Carries only the chest figures — never the player name or uid.
 */
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
