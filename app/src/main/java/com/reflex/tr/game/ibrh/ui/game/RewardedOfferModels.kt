package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.R

/** No daily cap: the offer is limited by the run or the streak instead of by a counter. */
internal const val REWARDED_OFFER_UNLIMITED = -1

/** What the player walks away with. Used for analytics and for picking the card's accent. */
enum class RewardedOfferKind {
    Coins,
    Continue,
    Chest,
    StreakProtect
}

/** Where an offer can be acted on. Everything is listed in Bonuses; only some can be taken there. */
enum class RewardedOfferSurface {
    Bonuses,
    GameOver
}

/**
 * Every rewarded-ad offer in the game, in one table.
 *
 * Each one maps onto a [RewardedAction] that already exists — this adds no new ad placement and no
 * second reward path. It is the presentation and eligibility layer over what the app already does,
 * so limits and payouts stay wherever they were already enforced.
 */
enum class RewardedOfferType(
    val storageKey: String,
    val action: RewardedAction,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val kind: RewardedOfferKind,
    val surface: RewardedOfferSurface
) {
    ContinueGame(
        storageKey = "continue_game",
        action = RewardedAction.Continue,
        titleRes = R.string.offer_continue_title,
        descriptionRes = R.string.offer_continue_description,
        kind = RewardedOfferKind.Continue,
        surface = RewardedOfferSurface.GameOver
    ),
    DoubleGameCoins(
        storageKey = "double_game_coins",
        action = RewardedAction.DoubleCoins,
        titleRes = R.string.offer_double_coins_title,
        descriptionRes = R.string.offer_double_coins_description,
        kind = RewardedOfferKind.Coins,
        surface = RewardedOfferSurface.GameOver
    ),
    FreeDailyChest(
        storageKey = "free_daily_chest",
        action = RewardedAction.CoinChest,
        titleRes = R.string.offer_free_chest_title,
        descriptionRes = R.string.offer_free_chest_description,
        kind = RewardedOfferKind.Chest,
        surface = RewardedOfferSurface.Bonuses
    ),
    BonusCoins(
        storageKey = "bonus_coins",
        action = RewardedAction.ShopCoinReward,
        titleRes = R.string.offer_bonus_coins_title,
        descriptionRes = R.string.offer_bonus_coins_description,
        kind = RewardedOfferKind.Coins,
        surface = RewardedOfferSurface.Bonuses
    ),
    StreakProtect(
        storageKey = "streak_protect",
        action = RewardedAction.ProtectStreak,
        titleRes = R.string.offer_streak_protect_title,
        descriptionRes = R.string.offer_streak_protect_description,
        kind = RewardedOfferKind.StreakProtect,
        surface = RewardedOfferSurface.Bonuses
    )
}

/**
 * Why an offer can or cannot be taken right now. Anything other than [Available] renders as a
 * short status line rather than a dead button — a greyed-out button reads as broken.
 */
enum class RewardedOfferAvailability {
    Available,
    LimitReached,
    NotApplicable,
    DuringGameOnly,
    AdNotReady
}

/**
 * One offer as the UI sees it. Every field is derived from state the app already keeps, so this
 * carries no stored data of its own and cannot drift from the system that pays the reward.
 */
@Immutable
data class RewardedOfferState(
    val type: RewardedOfferType,
    val availability: RewardedOfferAvailability,
    val remaining: Int = REWARDED_OFFER_UNLIMITED,
    val dailyLimit: Int = REWARDED_OFFER_UNLIMITED,
    val rewardCoins: Int = 0
) {
    val isAvailable: Boolean
        get() = availability == RewardedOfferAvailability.Available

    val hasDailyLimit: Boolean
        get() = dailyLimit > 0

    /** How many of today's chances are already spent, for the "1/2 used" line. */
    val usedToday: Int
        get() = (dailyLimit - remaining).coerceIn(0, dailyLimit.coerceAtLeast(0))
}
