package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.R

/** Lowest first. Distinct from [RankTier]: this resets weekly and reads only the week's points. */
enum class LeagueTier(
    val storageKey: String,
    @StringRes val titleRes: Int,
    val minPoints: Int,
    val rewardCoins: Int
) {
    Bronze("bronze", R.string.league_tier_bronze, 0, 100),
    Silver("silver", R.string.league_tier_silver, 500, 200),
    Gold("gold", R.string.league_tier_gold, 1_500, 350),
    Diamond("diamond", R.string.league_tier_diamond, 3_000, 500),
    Neon("neon", R.string.league_tier_neon, 5_000, 750);

    val next: LeagueTier?
        get() = entries.getOrNull(ordinal + 1)
}

/** [pendingRewardPoints] carries a finished week's uncollected reward; only a finished week pays. */
@Immutable
data class WeeklyLeagueState(
    val weekKey: String = "",
    val points: Int = 0,
    val pendingRewardPoints: Int = 0
) {
    val tier: LeagueTier
        get() = leagueTierForPoints(points)

    val pendingRewardTier: LeagueTier
        get() = leagueTierForPoints(pendingRewardPoints)

    val canClaimReward: Boolean
        get() = pendingRewardPoints > 0

    val pointsToNextTier: Int
        get() = tier.next?.let { (it.minPoints - points).coerceAtLeast(0) } ?: 0

    val tierProgressPercent: Int
        get() {
            val next = tier.next ?: return 100
            val span = (next.minPoints - tier.minPoints).coerceAtLeast(1)
            return (((points - tier.minPoints).coerceAtLeast(0) * 100) / span).coerceIn(0, 100)
        }
}
