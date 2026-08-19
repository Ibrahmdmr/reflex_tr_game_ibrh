package com.reflex.tr.game.ibrh.ui.game

import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseParam
import java.util.Calendar

/** Every rule the weekly league needs. UI reads state; only these functions decide it. */

private const val LEAGUE_COMBO_MULTIPLIER = 2
private const val LEAGUE_ACCURACY_BONUS = 25
private const val LEAGUE_ACCURACY_THRESHOLD = 80
private const val LEAGUE_NEW_RECORD_BONUS = 50
private const val LEAGUE_DAILY_EVENT_BONUS = 25

/** What one finished run did to the league standing. */
data class WeeklyLeagueAdvance(
    val state: WeeklyLeagueState,
    val earnedPoints: Int,
    val previousTier: LeagueTier,
    val upgradedTo: LeagueTier?
)

/** The band [points] falls into. Never fails: below every threshold means [LeagueTier.Bronze]. */
fun leagueTierForPoints(points: Int): LeagueTier {
    val safePoints = points.coerceAtLeast(0)
    return LeagueTier.entries.lastOrNull { safePoints >= it.minPoints } ?: LeagueTier.Bronze
}

/** League points for one finished run. Separate from coins — this feeds only the league. */
fun calculateLeaguePoints(
    score: Int,
    maxCombo: Int,
    accuracyPercent: Int,
    isNewBestScore: Boolean,
    dailyEventCompleted: Boolean
): Int {
    val base = score.coerceAtLeast(0)
    val comboBonus = maxCombo.coerceAtLeast(0) * LEAGUE_COMBO_MULTIPLIER
    val accuracyBonus = if (accuracyPercent >= LEAGUE_ACCURACY_THRESHOLD) LEAGUE_ACCURACY_BONUS else 0
    val recordBonus = if (isNewBestScore) LEAGUE_NEW_RECORD_BONUS else 0
    val eventBonus = if (dailyEventCompleted) LEAGUE_DAILY_EVENT_BONUS else 0
    return base + comboBonus + accuracyBonus + recordBonus + eventBonus
}

/**
 * This week's standing, rolling over when [state] is from an earlier week. Only the most recent
 * uncollected reward is carried, so no history builds up.
 */
fun weeklyLeagueForWeek(
    state: WeeklyLeagueState,
    weekKey: String = currentWeekKey()
): WeeklyLeagueState {
    if (state.weekKey == weekKey) return state
    return WeeklyLeagueState(
        weekKey = weekKey,
        points = 0,
        pendingRewardPoints = if (state.weekKey.isNotBlank() && state.points > 0) {
            state.points
        } else {
            state.pendingRewardPoints
        }
    )
}

/** Adds one finished run to this week's total. */
fun advanceWeeklyLeagueAfterGame(
    state: WeeklyLeagueState,
    score: Int,
    maxCombo: Int,
    accuracyPercent: Int,
    isNewBestScore: Boolean,
    dailyEventCompleted: Boolean,
    weekKey: String = currentWeekKey()
): WeeklyLeagueAdvance {
    val thisWeek = weeklyLeagueForWeek(state, weekKey)
    val earned = calculateLeaguePoints(
        score = score,
        maxCombo = maxCombo,
        accuracyPercent = accuracyPercent,
        isNewBestScore = isNewBestScore,
        dailyEventCompleted = dailyEventCompleted
    )
    if (earned <= 0) {
        return WeeklyLeagueAdvance(thisWeek, 0, thisWeek.tier, upgradedTo = null)
    }

    val previousTier = thisWeek.tier
    val nextPoints = (thisWeek.points.toLong() + earned.toLong())
        .coerceIn(0L, Int.MAX_VALUE.toLong())
        .toInt()
    val advanced = thisWeek.copy(points = nextPoints)
    return WeeklyLeagueAdvance(
        state = advanced,
        earnedPoints = earned,
        previousTier = previousTier,
        // Reports the band actually reached, so jumping straight to Neon announces Neon.
        upgradedTo = advanced.tier.takeIf { it.ordinal > previousTier.ordinal }
    )
}

/** Claims the waiting reward, returning null when there is nothing to collect. */
fun claimedWeeklyLeagueReward(state: WeeklyLeagueState): Pair<WeeklyLeagueState, LeagueTier>? {
    if (!state.canClaimReward) return null
    return state.copy(pendingRewardPoints = 0) to state.pendingRewardTier
}

/** Minutes until the league resets, for the "time left this week" line. */
fun weeklyLeagueRemainingMinutes(nowMillis: Long = System.currentTimeMillis()): Int {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        // Calendar can resolve "this Monday" to one already past, depending on the locale's
        // first day of week; step forward until the boundary is genuinely ahead.
        while (timeInMillis <= nowMillis) {
            add(Calendar.WEEK_OF_YEAR, 1)
        }
    }
    return ((calendar.timeInMillis - nowMillis).coerceAtLeast(0L) / 60_000L)
        .coerceAtMost(7L * 24L * 60L)
        .toInt()
}

/**
 * Weekly-league analytics. Carries only league figures — never the player name or uid.
 */
fun logWeeklyLeagueEvent(
    event: FirebaseEvent,
    tier: LeagueTier,
    totalPoints: Int,
    earnedPoints: Int = 0,
    rewardCoins: Int = 0
) {
    logGameEvent(event) {
        putString(FirebaseParam.League.key, tier.storageKey)
        putInt(FirebaseParam.TotalPoints.key, totalPoints.coerceAtLeast(0))
        putInt(FirebaseParam.PointsEarned.key, earnedPoints.coerceAtLeast(0))
        putInt(FirebaseParam.RewardCoin.key, rewardCoins.coerceAtLeast(0))
    }
}
