package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.runtime.Immutable

enum class DailyRewardType {
    Coins,
    SuperBox
}

val DailyRewardCoinPlan = listOf(50, 75, 100, 150, 200, 300, 500)

const val OneMoreGameBonusCoins = 25

const val FirstTargetBonusCoins = 50

private const val OneMoreGameBonusOfferLimit = 3

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
