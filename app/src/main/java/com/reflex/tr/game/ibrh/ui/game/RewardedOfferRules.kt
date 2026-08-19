package com.reflex.tr.game.ibrh.ui.game

import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseParam

/**
 * Every rule the rewarded offers need. UI reads state; only these functions decide it.
 *
 * None of this grants anything. Each offer still runs through the [RewardedAction] that already
 * owned it, so the ad callback remains the only thing that can pay out.
 */

/** At most this many rewarded actions on the Game Over panel, so it stays a result screen. */
internal const val MAX_GAME_OVER_OFFERS = 2

/** The ad is usable when one is loaded and none is on screen. */
private fun RewardedAdUiState.canStart(): Boolean = isReady && !isShowing

/**
 * The Bonuses list: every offer, with the ones that only make sense mid-run marked as such rather
 * than hidden. Seeing what a run can earn is the point of the section.
 */
internal fun bonusOffers(
    progression: ProgressionState,
    rewardedAdUiState: RewardedAdUiState
): List<RewardedOfferState> = RewardedOfferType.entries.map { type ->
    when (type) {
        RewardedOfferType.FreeDailyChest -> progression.coinChest.let { chest ->
            RewardedOfferState(
                type = type,
                availability = when {
                    !chest.canOpen -> RewardedOfferAvailability.LimitReached
                    !rewardedAdUiState.canStart() -> RewardedOfferAvailability.AdNotReady
                    else -> RewardedOfferAvailability.Available
                },
                remaining = chest.remainingOpens,
                dailyLimit = chest.maxOpensPerDay
            )
        }

        RewardedOfferType.BonusCoins -> progression.shopCoinReward.let { reward ->
            RewardedOfferState(
                type = type,
                availability = when {
                    !reward.canClaim -> RewardedOfferAvailability.LimitReached
                    !rewardedAdUiState.canStart() -> RewardedOfferAvailability.AdNotReady
                    else -> RewardedOfferAvailability.Available
                },
                remaining = reward.remainingClaims,
                dailyLimit = reward.maxClaimsPerDay,
                rewardCoins = reward.rewardCoins
            )
        }

        RewardedOfferType.StreakProtect -> RewardedOfferState(
            type = type,
            // Not a limit but a condition: there is nothing to protect until the streak is at risk.
            availability = when {
                !progression.dailyReward.canProtectStreak -> RewardedOfferAvailability.NotApplicable
                !rewardedAdUiState.canStart() -> RewardedOfferAvailability.AdNotReady
                else -> RewardedOfferAvailability.Available
            }
        )

        RewardedOfferType.ContinueGame,
        RewardedOfferType.DoubleGameCoins -> RewardedOfferState(
            type = type,
            availability = RewardedOfferAvailability.DuringGameOnly
        )
    }
}

/**
 * The rewarded actions the Game Over panel may show, in priority order and capped at
 * [MAX_GAME_OVER_OFFERS]. Anything unavailable is left out entirely rather than disabled.
 *
 * Continue comes first because it is the only one that changes the run; doubling the payout is
 * still there afterwards, when continuing is no longer on the table.
 */
internal fun gameOverRewardedOffers(
    canContinue: Boolean,
    isContinueReady: Boolean,
    baseCoinsThisGame: Int,
    isCoinDoubleClaimed: Boolean,
    rewardedAdUiState: RewardedAdUiState
): List<RewardedOfferState> = buildList {
    // A ready reward survives an ad that has since unloaded: it is already paid for.
    if (canContinue && (isContinueReady || rewardedAdUiState.canStart())) {
        add(
            RewardedOfferState(
                type = RewardedOfferType.ContinueGame,
                availability = RewardedOfferAvailability.Available
            )
        )
    }
    if (!isCoinDoubleClaimed && baseCoinsThisGame > 0 && rewardedAdUiState.canStart()) {
        add(
            RewardedOfferState(
                type = RewardedOfferType.DoubleGameCoins,
                availability = RewardedOfferAvailability.Available,
                rewardCoins = baseCoinsThisGame
            )
        )
    }
}.take(MAX_GAME_OVER_OFFERS)

/**
 * Rewarded-offer analytics. Carries only the offer's own identity and figures — never the player name or uid.
 */
internal fun logRewardedOfferEvent(
    event: FirebaseEvent,
    type: RewardedOfferType? = null,
    rewardCoins: Int = 0,
    source: String? = null,
    reason: String? = null,
    isPremium: Boolean = false
) {
    logGameEvent(event) {
        type?.let {
            putString(FirebaseParam.OfferType.key, it.storageKey)
            putString(FirebaseParam.RewardType.key, it.kind.name)
        }
        putInt(FirebaseParam.RewardCoin.key, rewardCoins.coerceAtLeast(0))
        source?.let { putString(FirebaseParam.Source.key, it) }
        reason?.let { putString(FirebaseParam.Reason.key, it) }
        putBoolean(FirebaseParam.IsPremium.key, isPremium)
    }
}
