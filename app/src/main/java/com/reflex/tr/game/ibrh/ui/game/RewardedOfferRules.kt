package com.reflex.tr.game.ibrh.ui.game

import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseParam

internal const val MAX_GAME_OVER_OFFERS = 2

private fun RewardedAdUiState.canStart(): Boolean = isReady && !isShowing

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
            // Not a limit but a condition: nothing to protect until the streak is at risk.
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

/** Continue comes first: it is the only offer that changes the run. */
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

/** Carries only the offer's own identity and figures — never playerName or uid. */
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
