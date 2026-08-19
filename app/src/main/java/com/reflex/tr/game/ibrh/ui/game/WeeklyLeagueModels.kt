package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.R

/**
 * Weekly league bands, lowest first. Distinct from [RankTier], which tracks lifetime level: this
 * one resets weekly and is driven only by the points earned within the week.
 */
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

    /** The next band up, or null when this is already the top. */
    val next: LeagueTier?
        get() = entries.getOrNull(ordinal + 1)
}

/**
 * This week's standing. [pendingRewardPoints] carries a finished week whose reward went
 * uncollected; only a finished week pays out, so it cannot be banked twice in one week.
 */
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

    /** Points still needed for [LeagueTier.next], or 0 at the top band. */
    val pointsToNextTier: Int
        get() = tier.next?.let { (it.minPoints - points).coerceAtLeast(0) } ?: 0

    /** Progress through the current band, 0..100. Always 100 once the top band is reached. */
    val tierProgressPercent: Int
        get() {
            val next = tier.next ?: return 100
            val span = (next.minPoints - tier.minPoints).coerceAtLeast(1)
            return (((points - tier.minPoints).coerceAtLeast(0) * 100) / span).coerceIn(0, 100)
        }
}
