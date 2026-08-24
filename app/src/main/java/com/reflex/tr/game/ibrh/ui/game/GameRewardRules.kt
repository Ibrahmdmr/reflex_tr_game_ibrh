package com.reflex.tr.game.ibrh.ui.game

internal const val FLAWLESS_STREAK_5_BONUS_COINS = 10
internal const val FLAWLESS_STREAK_10_BONUS_COINS = 25
internal const val FLAWLESS_STREAK_20_BONUS_COINS = 75
internal val BOSS_ROUND_SCORE_THRESHOLDS = setOf(15, 30, 50)
internal val ULTRA_MOMENT_COMBO_THRESHOLDS = setOf(10, 20)

internal fun flawlessStreakBonusFor(streak: Int): Int {
    return when (streak) {
        5 -> FLAWLESS_STREAK_5_BONUS_COINS
        10 -> FLAWLESS_STREAK_10_BONUS_COINS
        20 -> FLAWLESS_STREAK_20_BONUS_COINS
        else -> 0
    }
}

internal fun bossRoundThresholdForScore(
    newScore: Int,
    triggeredThresholds: Set<Int>
): Int? {
    return BOSS_ROUND_SCORE_THRESHOLDS
        .filter { it !in triggeredThresholds && newScore >= it }
        .minOrNull()
}

internal fun ultraMomentThresholdForCombo(
    combo: Int,
    triggeredThresholds: Set<Int>
): Int? {
    return ULTRA_MOMENT_COMBO_THRESHOLDS
        .filter { it !in triggeredThresholds && combo >= it }
        .minOrNull()
}

internal fun calculateBonusHourCoins(
    baseCoins: Int,
    bonusHour: BonusHourState
): Int {
    if (!bonusHour.isActive || baseCoins <= 0) return 0

    return (baseCoins * bonusHour.coinBonusPercent / 100).coerceAtLeast(1)
}

internal fun accuracyPercent(hits: Int, attempts: Int): Int {
    val safeAttempts = attempts.coerceAtLeast(0)
    if (safeAttempts == 0) return 0
    return ((hits.coerceAtLeast(0) * 100f) / safeAttempts).toInt().coerceIn(0, 100)
}

internal fun sanitizePlayerName(name: String): String? {
    val cleanedName = name.trim().take(12)
    if (cleanedName.isBlank()) return null

    val loweredName = cleanedName.lowercase()
    val blockedTerms = listOf("amk", "aq", "oros", "sik", "fuck", "shit")
    if (blockedTerms.any { loweredName.contains(it) }) return null

    return cleanedName
}

internal fun consumeTrialThemeGame(progression: ProgressionState): ProgressionState {
    if (progression.trialGamesRemaining <= 0) return progression

    val remaining = progression.trialGamesRemaining - 1
    return progression.copy(
        trialGamesRemaining = remaining,
        trialTheme = progression.trialTheme.takeIf { remaining > 0 }
    )
}
