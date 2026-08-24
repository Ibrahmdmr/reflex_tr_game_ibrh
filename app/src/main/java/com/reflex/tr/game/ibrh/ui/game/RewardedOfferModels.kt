package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.R

/** No daily cap: limited by the run or the streak instead of by a counter. */
internal const val REWARDED_OFFER_UNLIMITED = -1

enum class RewardedOfferKind {
    Coins,
    Continue,
    Chest,
    StreakProtect
}

enum class RewardedOfferSurface {
    Bonuses,
    GameOver
}

/** Each maps onto an existing [RewardedAction]: no new placement, no second reward path. */
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

enum class RewardedOfferAvailability {
    Available,
    LimitReached,
    NotApplicable,
    DuringGameOnly,
    AdNotReady
}

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

    val usedToday: Int
        get() = (dailyLimit - remaining).coerceIn(0, dailyLimit.coerceAtLeast(0))
}
