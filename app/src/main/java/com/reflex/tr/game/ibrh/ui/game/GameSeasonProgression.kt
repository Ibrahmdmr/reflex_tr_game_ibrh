package com.reflex.tr.game.ibrh.ui.game

import java.util.Calendar

private const val DAILY_MODE_COIN_BONUS_PERCENT = 20
private const val DAILY_MINI_TOURNAMENT_REWARD_COINS = 150
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
    return dateFormatter().format(Calendar.getInstance().time)
}

/** ISO-style `yyyy-Www` key identifying the current week. */
internal fun currentWeekKey(nowMillis: Long = System.currentTimeMillis()): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = nowMillis }
    return "${calendar.get(Calendar.YEAR)}-W${calendar.get(Calendar.WEEK_OF_YEAR)}"
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

internal fun createDailyMiniTournamentState(
    dateKey: String = todayDateKey(),
    bestScore: Int = 0,
    claimed: Boolean = false
): DailyMiniTournamentState {
    val mode = dailyMiniTournamentModeForToday()
    val targetScore = dailyMiniTournamentTargetFor(mode)
    val safeBestScore = bestScore.coerceAtLeast(0)
    return DailyMiniTournamentState(
        dateKey = dateKey,
        mode = mode,
        bestScore = safeBestScore,
        targetScore = targetScore,
        rewardCoins = DAILY_MINI_TOURNAMENT_REWARD_COINS,
        completed = safeBestScore >= targetScore,
        claimed = claimed && safeBestScore >= targetScore
    )
}

internal fun advanceDailyMiniTournamentAfterGame(
    tournament: DailyMiniTournamentState,
    playedMode: GameMode,
    score: Int
): DailyMiniTournamentState {
    val todayTournament = if (tournament.dateKey == todayDateKey()) {
        tournament
    } else {
        createDailyMiniTournamentState()
    }
    if (playedMode != todayTournament.mode) return todayTournament.copy(rewardClaimedThisGame = false)

    val nextBestScore = maxOf(todayTournament.bestScore, score.coerceAtLeast(0))
    val completed = nextBestScore >= todayTournament.targetScore
    val shouldClaimReward = completed && !todayTournament.claimed
    return todayTournament.copy(
        bestScore = nextBestScore,
        completed = completed,
        claimed = todayTournament.claimed || shouldClaimReward,
        rewardClaimedThisGame = shouldClaimReward
    )
}

internal fun dailyMiniTournamentRewardForGame(
    tournament: DailyMiniTournamentState,
    playedMode: GameMode,
    score: Int
): Int {
    val todayTournament = if (tournament.dateKey == todayDateKey()) {
        tournament
    } else {
        createDailyMiniTournamentState()
    }
    if (playedMode != todayTournament.mode || todayTournament.claimed) return 0
    return if (score.coerceAtLeast(0) >= todayTournament.targetScore) {
        todayTournament.rewardCoins.coerceAtLeast(0)
    } else {
        0
    }
}

internal fun dailyMiniTournamentTargetFor(mode: GameMode): Int {
    return when (mode) {
        GameMode.Classic -> 35
        GameMode.MovingTarget -> 30
        GameMode.FakeTarget -> 20
        GameMode.ColorReflex -> 25
    }
}

private fun dailyMiniTournamentModeForToday(): GameMode {
    return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> GameMode.Classic
        Calendar.TUESDAY -> GameMode.MovingTarget
        Calendar.WEDNESDAY -> GameMode.FakeTarget
        Calendar.THURSDAY -> GameMode.ColorReflex
        Calendar.FRIDAY -> GameMode.Classic
        Calendar.SATURDAY -> GameMode.MovingTarget
        else -> GameMode.FakeTarget
    }
}

internal fun calculateDailyModeBonusCoins(
    baseCoins: Int,
    playedMode: GameMode,
    dailyFeaturedMode: DailyFeaturedModeState
): Int {
    if (baseCoins <= 0 || playedMode != dailyFeaturedMode.mode) return 0
    return (baseCoins * dailyFeaturedMode.coinBonusPercent / 100).coerceAtLeast(1)
}
