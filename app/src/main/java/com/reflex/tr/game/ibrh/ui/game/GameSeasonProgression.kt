package com.reflex.tr.game.ibrh.ui.game

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val DAILY_MODE_COIN_BONUS_PERCENT = 20
private const val SEASON_XP_BOOST_PERCENT_BASE = 100

internal fun addSeasonXp(
    progression: ProgressionState,
    xpAmount: Int,
    countGamePlayed: Boolean = false,
    countRewardedAd: Boolean = false,
    currentTimeMillis: Long = System.currentTimeMillis()
): ProgressionState {
    if (xpAmount <= 0) return progression
    val season = seasonForToday(progression.season)
    val maxXp = (SeasonMaxLevel - 1) * SeasonXpPerLevel
    val boostedXp = if (season.xpBoostEndTimeMillis > currentTimeMillis) {
        (xpAmount * (SEASON_XP_BOOST_PERCENT_BASE + SeasonXpBoostBonusPercent)) / SEASON_XP_BOOST_PERCENT_BASE
    } else {
        xpAmount
    }.coerceAtLeast(0)
    return progression.copy(
        season = season.copy(
            xp = (season.xp + boostedXp).coerceIn(0, maxXp),
            gamesPlayedToday = season.gamesPlayedToday + if (countGamePlayed) 1 else 0,
            rewardedAdsWatchedToday = season.rewardedAdsWatchedToday + if (countRewardedAd) 1 else 0,
            seasonXpEarnedToday = season.seasonXpEarnedToday + boostedXp
        )
    )
}

internal fun seasonForToday(season: SeasonState): SeasonState {
    val today = todayDateKey()
    return if (season.missionDateKey == today) {
        season
    } else {
        season.copy(
            missionDateKey = today,
            gamesPlayedToday = 0,
            rewardedAdsWatchedToday = 0,
            seasonXpEarnedToday = 0,
            claimedMissionIds = emptySet()
        )
    }
}

internal fun todayDateKey(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
}

internal fun createDailyFeaturedMode(dateKey: String = todayDateKey()): DailyFeaturedModeState {
    val modes = GameMode.entries
    val index = Math.floorMod(dateKey.hashCode(), modes.size)
    return DailyFeaturedModeState(
        dateKey = dateKey,
        mode = modes[index],
        coinBonusPercent = DAILY_MODE_COIN_BONUS_PERCENT
    )
}

internal fun calculateDailyModeBonusCoins(
    baseCoins: Int,
    playedMode: GameMode,
    dailyFeaturedMode: DailyFeaturedModeState
): Int {
    if (baseCoins <= 0 || playedMode != dailyFeaturedMode.mode) return 0
    return (baseCoins * dailyFeaturedMode.coinBonusPercent / 100).coerceAtLeast(1)
}
